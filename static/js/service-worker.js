/// <reference no-default-lib="true"/>
/// <reference lib="es2015" />
/// <reference lib="webworker" />

/**
 * @type {ServiceWorkerGlobalScope}
 */
const sw = self

// Push subscription event listener
sw.addEventListener('push', event => {
  const numEvents = event.data.json()
  sw.registration.showNotification('Beat the Queue', {
    body: `${numEvents} Upcoming events (click to show)`,
    icon: 'icons/icon.svg',
    vibrate: [100, 50, 100],
    requireInteraction: true,
    data: {
      url: `${sw.location.origin}/events`
    }
  })
})

// Notification click event listener
sw.addEventListener('notificationclick', event => {
  const url = event.notification.data.url
  // Close the notification popout
  event.notification.close()
  // Get all the Window clients
  event.waitUntil(
    sw.clients
      .matchAll({ type: 'window' })
      .then(clientsArr => {
        // If a Window tab matching the targeted URL already exists, focus that;
        const hadWindowToFocus = clientsArr.some(
          windowClient => windowClient.url === url
            ? (windowClient.focus(), true)
            : false)
        // Otherwise, open a new tab to the applicable URL and focus it.
        if (!hadWindowToFocus) {
          sw.clients
            .openWindow(url)
            .then(windowClient =>
              windowClient
                ? windowClient.focus()
                : null)
        }
      }))
})
