# واجهة رفع الوسائط V6

في Android استخدم `FirebaseStorageRepository`:
- `uploadProductImage(uri)` ثم مرر الرابط إلى `publishProduct(..., imageUrl)`.
- `uploadTopUpReceipt(uri)` ثم مرر الرابط إلى `createTopUpRequest(..., receiptImage)`.
- `uploadDeliveryProof(uri)` ثم مرر الرابط إلى `submitDeliveryProof(..., proof)`.

يجب أن يكون الرابط ناتجاً من Firebase Storage. لا تعتمد وظائف الخادم على روابط خارجية غير موثوقة.
