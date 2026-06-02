const AMAP_KEY = import.meta.env.VITE_AMAP_KEY || ''
const AMAP_SECRET = import.meta.env.VITE_AMAP_SECRET || ''

let amapLoadPromise = null

function loadAMapScript() {
  if (amapLoadPromise) return amapLoadPromise

  amapLoadPromise = new Promise((resolve, reject) => {
    if (window.AMap) {
      resolve(window.AMap)
      return
    }

    if (!AMAP_KEY || AMAP_KEY === 'YOUR_AMAP_KEY') {
      reject(new Error('高德地图Key未配置，请在 .env 文件中设置 VITE_AMAP_KEY'))
      return
    }

    if (AMAP_SECRET && AMAP_SECRET !== 'YOUR_AMAP_SECRET') {
      window._AMapSecurityConfig = {
        securityJsCode: AMAP_SECRET
      }
    }

    const script = document.createElement('script')
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${AMAP_KEY}`
    script.async = true
    script.onload = () => {
      if (window.AMap) {
        resolve(window.AMap)
      } else {
        reject(new Error('高德地图API加载失败'))
      }
    }
    script.onerror = () => {
      reject(new Error('高德地图脚本加载失败，请检查网络连接'))
    }
    document.head.appendChild(script)
  })

  return amapLoadPromise
}

export async function initAMapMap(containerId, options = {}) {
  const AMap = await loadAMapScript()
  return new AMap.Map(containerId, {
    zoom: 15,
    center: [116.397428, 39.90923],
    resizeEnable: true,
    ...options
  })
}

export async function getAMapGeolocation(mapInstance) {
  const AMap = await loadAMapScript()

  return new Promise((resolve, reject) => {
    AMap.plugin('AMap.Geolocation', () => {
      const geolocation = new AMap.Geolocation({
        enableHighAccuracy: true,
        timeout: 10000,
        showButton: false,
        showMarker: true,
        showCircle: true,
        panToLocation: true,
        zoomToAccuracy: true
      })
      mapInstance.addControl(geolocation)
      geolocation.getCurrentPosition((status, result) => {
        if (status === 'complete') {
          resolve({
            lng: result.position.lng,
            lat: result.position.lat,
            address: result.formattedAddress || `${result.position.lng},${result.position.lat}`
          })
        } else {
          reject(new Error(result.message || '高德定位失败'))
        }
      })
    })
  })
}

export async function reverseGeocode(lng, lat) {
  const AMap = await loadAMapScript()

  return new Promise((resolve, reject) => {
    AMap.plugin('AMap.Geocoder', () => {
      const geocoder = new AMap.Geocoder({
        city: '全国'
      })
      geocoder.getAddress([lng, lat], (status, result) => {
        if (status === 'complete' && result.regeocode) {
          resolve(result.regeocode.formattedAddress)
        } else {
          reject(new Error('地址解析失败'))
        }
      })
    })
  })
}

export function getBrowserGeolocation() {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error('浏览器不支持定位功能'))
      return
    }
    navigator.geolocation.getCurrentPosition(
      (position) => {
        resolve({
          lng: position.coords.longitude,
          lat: position.coords.latitude,
          address: `${position.coords.longitude.toFixed(6)}, ${position.coords.latitude.toFixed(6)}`
        })
      },
      (error) => {
        const messages = {
          1: '用户拒绝了定位请求',
          2: '位置信息不可用',
          3: '定位请求超时'
        }
        reject(new Error(messages[error.code] || '浏览器定位失败'))
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
    )
  })
}

export async function getLocation(mapInstance) {
  try {
    const result = await getAMapGeolocation(mapInstance)
    try {
      const address = await reverseGeocode(result.lng, result.lat)
      result.address = address
    } catch (geoError) {
      console.warn('高德逆地理编码失败，使用原始地址:', geoError.message)
      if (!result.address || result.address.includes(',')) {
        result.address = `${result.lng.toFixed(6)}, ${result.lat.toFixed(6)}`
      }
    }
    return result
  } catch (amapError) {
    console.warn('高德定位失败，尝试浏览器原生定位:', amapError.message)
    try {
      const result = await getBrowserGeolocation()
      try {
        const address = await reverseGeocode(result.lng, result.lat)
        result.address = address + '（浏览器定位）'
      } catch (geoError) {
        console.warn('地址解析失败，使用坐标显示:', geoError.message)
        result.address = `${result.lng.toFixed(6)}, ${result.lat.toFixed(6)}（浏览器定位）`
      }
      return result
    } catch (browserError) {
      throw new Error(`定位失败: ${amapError.message}；浏览器定位也失败: ${browserError.message}`, { cause: browserError })
    }
  }
}
