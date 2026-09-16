# إعداد Firebase — المتجر اليمني

تم تحويل تسجيل الدخول/التسجيل إلى **Firebase Authentication + Cloud Firestore**.
كلمات المرور لا تُحفظ في Room أو Firestore.

## 1) إنشاء مشروع Firebase
1. افتح Firebase Console وأنشئ مشروعاً للمتجر اليمني.
2. أضف Android App بالحزمة:
   `com.aistudio.yemenistore.ymkt`
3. نزّل `google-services.json` وضعه داخل:
   `app/google-services.json`
4. في Firebase Authentication فعّل:
   **Sign-in method → Email/Password**.

## 2) Firestore
أنشئ قاعدة Firestore.
المستخدمون يحفظون في:
`users/{firebaseUid}`

الحقول الأساسية:
- `id`
- `email`
- `fullName`
- `phone`
- `role`: `CONSUMER` أو `MERCHANT` أو `PROMOTER` أو `ADMIN`
- `isEmailVerified`
- `isFrozen`
- `freezeReason`
- `promoCode`
- `referredByPromoCode`
- `jeebAccountNumber`
- `createdAt`

## 3) الإدارة
لا يمكن للمستخدم إنشاء حساب ADMIN من شاشة التسجيل.
أنشئ حساب الإدارة من Firebase Authentication، ثم أنشئ وثيقة المستخدم في Firestore واجعل:
`role = ADMIN`

## 4) قواعد Firestore الأولية
لا تترك Firestore مفتوحاً في الإنتاج. استخدم قواعد تمنع المستخدم من تغيير دوره أو حالة التجميد بنفسه، وتسمح للإدارة فقط بتغيير بيانات الإدارة.

## ملاحظة مهمة
هذه النسخة تنقل **المصادقة وملفات المستخدمين** إلى Firebase. الخطوة التالية هي نقل المنتجات والطلبات والمحافظ والمعاملات وعمليات الـEscrow إلى Firestore/Cloud Functions حتى تصبح دورة التجارة كاملة حقيقية ومتزامنة بين الأجهزة. لا ينبغي تشغيل النظام المالي الحقيقي اعتماداً على Room المحلي وحده.

## Firestore Rules
انشر ملف `firestore.rules` في مشروع Firebase قبل الاختبار الإنتاجي. القواعد تمنع المستخدم العادي من الكتابة المباشرة إلى المحافظ والمعاملات والسجلات المالية.

## ملاحظة مهمة
طبقة العرض أصبحت تقرأ بيانات المتجر من Firestore، وتتم مزامنة نتائج العمليات الحالية إلى Firestore. قبل الإنتاج يجب نقل منطق التسوية المالي بالكامل إلى Cloud Functions/Transaction موثوق به حتى لا تعتمد الأموال على جهاز العميل.
