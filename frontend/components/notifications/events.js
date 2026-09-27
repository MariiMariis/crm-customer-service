export const NOTIFICATIONS_CHANGED = "nexo:notifications-changed";

export function notifyNotificationsChanged() {
    if (typeof window !== "undefined") window.dispatchEvent(new Event(NOTIFICATIONS_CHANGED));
}
