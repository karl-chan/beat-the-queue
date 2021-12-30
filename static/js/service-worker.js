self.addEventListener('push', function (e) {
  const options = {
    body: 'Upcoming events',
    icon: 'images/example.png',
    vibrate: [100, 50, 100],
    data: {
      dateOfArrival: Date.now(),
      primaryKey: '2'
    }
  }
  e.waitUntil(
    self.registration.showNotification('[Beat the Queue]', options)
  )
})

self.addEventListener('notificationclick', function (e) {
  const notification = e.notification
  const action = e.action

  // TODO: Open results page
})
