# إعداد الخادم المالي الآمن

تم نقل عمليتي الشراء/حجز Escrow وتحرير الضمان وتسوية الحصص إلى Firebase Cloud Functions.

## النشر
1. ثبّت Firebase CLI.
2. سجّل الدخول: `firebase login`
3. من مجلد المشروع الذي يحتوي `firebase.json` نفّذ:
   `firebase use <PROJECT_ID>`
4. ادخل إلى `functions` ثم نفّذ `npm install`.
5. نفّذ من جذر المشروع:
   `firebase deploy --only functions,firestore:rules`

## مهم
- لا تضع مفاتيح الخدمة داخل تطبيق Android.
- عمليات المحافظ والطلبات والمعاملات المالية لا تسمح لها قواعد Firestore بالكتابة من العميل.
- Cloud Functions هي الجهة الوحيدة التي تعدّل أرصدة Escrow والتسويات.
- قبل الإنتاج يجب ربط Firebase App Check والتحقق من إعدادات الحسابات/الإيصالات وفق بوابة جيب الفعلية.
- يجب اختبار قواعد Firestore وFunctions في Firebase Emulator قبل الإطلاق.
