# بناء APK من الهاتف

1. أنشئ حساب GitHub وافتح github.com من الهاتف.
2. أنشئ مستودعًا جديدًا، ثم ارفع كامل ملفات المشروع إلى المستودع.
3. ارفع الملفات والمجلدات كما هي، بما فيها `.github/workflows/build-apk.yml`.
4. من تبويب Actions اختر **Build Android APK** ثم **Run workflow**.
5. بعد نجاح البناء افتح نتيجة التشغيل ثم قسم **Artifacts**.
6. نزّل `yemeni-store-debug-apk`، فك الضغط، ثم ثبّت ملف APK على هاتف Android.

## Firebase
لجعل التطبيق يتصل بمشروع Firebase الحقيقي، ضع محتوى `google-services.json` في GitHub Secret باسم:
`GOOGLE_SERVICES_JSON_B64`

وقيمة السر هي محتوى الملف بعد تحويله إلى Base64. لا ترفع `google-services.json` مباشرة إلى مستودع عام.
