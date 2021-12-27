async function main () {
  if ('serviceWorker' in navigator) {
    try {
      const registration = await navigator.serviceWorker.register('/static/js/service-worker.js')
      console.log('Registration successful, scope is:', registration.scope)
    } catch (err) {
      console.error('Service worker registration failed, error:', err)
    }
  }
}

main()
