# المتجر اليمني — Secure V6

## الصور والملفات
- `product_images/{uid}/...` صور المنتجات.
- `topup_receipts/{uid}/...` إيصالات شحن جيب.
- `delivery_proofs/{uid}/...` إثباتات التسليم.
- الحجم الأقصى 10MB ونوع الملف يجب أن يكون صورة.
- لا تضع مفاتيح Service Account داخل Android.

## الإشعارات FCM
التطبيق يسجل رمز FCM للمستخدم في `users/{uid}/fcm_tokens/{token}`. خدمة Firebase ترسل الإشعار من الخادم فقط.

## نشر القواعد
```bash
firebase deploy --only storage,firestore,functions
```

## صلاحيات Android 13+
يجب طلب `POST_NOTIFICATIONS` من واجهة التطبيق وقت التشغيل قبل الاعتماد على الإشعارات.

## ملاحظة
رفع الصورة بحد ذاته لا ينشئ منتجاً أو يغير رصيداً. إنشاء المنتج والشحن والتسويات تبقى عمليات Cloud Functions محمية.
