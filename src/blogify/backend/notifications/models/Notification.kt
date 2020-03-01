package blogify.backend.notifications.models

import blogify.backend.resources.models.Resource

data class Notification<TEmitter, TSource, TSubject> (
    val emitter:  TEmitter,
    val source:   TSource,
    val subjects: Set<TSubject>
) where TEmitter : NotificationEmitter, TEmitter : Resource,
        TSource  : NotificationSource,  TSource  : Resource,
        TSubject : NotificationSubject, TSubject : Resource

