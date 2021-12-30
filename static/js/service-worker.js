/// <reference no-default-lib="true"/>
/// <reference lib="es2015" />
/// <reference lib="webworker" />

/**
 * @type {ServiceWorkerGlobalScope}
 */
const sw = self

sw.addEventListener('push', event => {
  const notification = new Notification('Beat the Queue', {
    body: 'Upcoming events',
    icon: 'icons/icon.svg',
    vibrate: [100, 50, 100],
    data: event.data.json()
  })
  notification.addEventListener('click', event => {})
})
