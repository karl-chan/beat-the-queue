async function main () {
  const registration = await registerServiceWorker()
  await requestPermissions()
  if (registration) {
    await subscribePushNotifications(registration)
  }
}

async function registerServiceWorker () {
  if ('serviceWorker' in navigator) {
    try {
      const registration = await navigator.serviceWorker.register('/static/js/service-worker.js')
      console.log('Registration successful, scope is:', registration.scope)
      return registration
    } catch (err) {
      console.error('Service worker registration failed, error:', err)
    }
  }
  return undefined
}

async function requestPermissions () {
  const status = await Notification.requestPermission()
  console.log('Notification permission status:', status)
}

async function subscribePushNotifications (/** @type {ServiceWorkerRegistration} */ registration) {
  const applicationServerKey = await fetch('/api/service-worker/vapid-public-key')
    .then(res => res.text())

  try {
    const pushSubscription = await registration.pushManager.subscribe({
      applicationServerKey,
      userVisibleOnly: true
    })
    console.log('Push subcription: ', pushSubscription)

    const el = document.getElementById('pushSubscriptionEndpoint')
    if (el) {
      el.value = pushSubscription.endpoint
      console.log('Sucessfully subscribed to push')
    } else {
      console.error('Unable to find pushSubscriptionEndpoint hidden input field')
    }
  } catch (err) {
    if (Notification.permission === 'denied') {
      console.error('Permission for notifications was denied')
    } else {
      console.error('Unable to subscribe to push', err)
    }
  }
}

main()
