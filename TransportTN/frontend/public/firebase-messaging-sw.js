/*
 * Service worker des notifications push TransportTN.
 * Le serveur envoie des messages FCM « data » ({ titre, message, type, lien }) ;
 * ce fichier les affiche quand l'application n'est pas à l'écran.
 * Quand l'application est au premier plan, la cloche (WebSocket) s'en charge.
 */
self.addEventListener('install', () => self.skipWaiting());
self.addEventListener('activate', (event) => event.waitUntil(self.clients.claim()));

self.addEventListener('push', (event) => {
  let donnees = {};
  try {
    donnees = event.data?.json()?.data ?? {};
  } catch {
    // message illisible : on affiche quand même une notification générique
  }

  event.waitUntil((async () => {
    const fenetres = await self.clients.matchAll({ type: 'window', includeUncontrolled: true });
    if (fenetres.some((fenetre) => fenetre.focused)) return;   // l'application est à l'écran

    await self.registration.showNotification(donnees.titre ?? 'TransportTN', {
      body: donnees.message ?? '',
      icon: '/favicon.svg',
      lang: 'fr',
      data: { lien: donnees.lien ?? '/' },
    });
  })());
});

// Clic sur la notification : on ouvre (ou on réutilise) un onglet sur la bonne page
self.addEventListener('notificationclick', (event) => {
  event.notification.close();
  const url = new URL(event.notification.data?.lien ?? '/', self.location.origin).href;

  event.waitUntil((async () => {
    const fenetres = await self.clients.matchAll({ type: 'window', includeUncontrolled: true });
    for (const fenetre of fenetres) {
      if (fenetre.url.startsWith(self.location.origin)) {
        await fenetre.focus();
        try {
          await fenetre.navigate(url);
        } catch {
          // onglet non contrôlé par ce service worker : il reste simplement au premier plan
        }
        return;
      }
    }
    await self.clients.openWindow(url);
  })());
});