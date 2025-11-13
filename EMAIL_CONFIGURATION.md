# 📧 دليل تكوين البريد الإلكتروني - Email Configuration Guide

## 🎯 نظرة عامة

هذا الدليل يشرح كيفية تكوين إرسال التنبيهات التلقائية عبر البريد الإلكتروني في نظام SupplyChainX.

---

## ⚙️ الخيارات المتاحة

### الخيار 1: Gmail (موصى به للتطوير) ✅

#### 1. إنشاء App Password من Gmail

1. اذهب إلى: https://myaccount.google.com/security
2. فعّل **Two-Factor Authentication** (2FA)
3. اذهب إلى: **App Passwords** → https://myaccount.google.com/apppasswords
4. اختر **Mail** و **Other (Custom name)**
5. انسخ الـ **App Password** (16 حرف بدون مسافات)

#### 2. تكوين المتغيرات البيئية

أضف في `start-app.sh`:

```bash
#!/bin/bash
export DB_USERNAME=supplychainx_user
export DB_PASSWORD=supplychainx_password

# Gmail Configuration
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=abcd-efgh-ijkl-mnop  # App Password من الخطوة السابقة

# Alert Configuration
export ALERT_EMAIL_ENABLED=true
export ALERT_EMAIL_TO=admin@supplychainx.com  # البريد الذي سيستقبل التنبيهات

mvn spring-boot:run -pl supplychainx-app
```

#### 3. التكوين في application.yml (موجود مسبقاً)

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME:your-email@gmail.com}
    password: ${MAIL_PASSWORD:your-app-password}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
          connectiontimeout: 5000
          timeout: 5000
          writetimeout: 5000

app:
  alert:
    email:
      enabled: ${ALERT_EMAIL_ENABLED:false}
      to: ${ALERT_EMAIL_TO:admin@supplychainx.com}
```

---

### الخيار 2: Microsoft Outlook / Office 365

#### تكوين المتغيرات:

```bash
export MAIL_USERNAME=your-email@outlook.com
export MAIL_PASSWORD=your-password
export ALERT_EMAIL_ENABLED=true
export ALERT_EMAIL_TO=admin@supplychainx.com
```

#### تعديل application.yml:

```yaml
spring:
  mail:
    host: smtp-mail.outlook.com  # أو smtp.office365.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
          ssl:
            trust: smtp-mail.outlook.com
```

---

### الخيار 3: SMTP مخصص (Corporate Email)

#### مثال لـ SendGrid:

```bash
export MAIL_USERNAME=apikey
export MAIL_PASSWORD=SG.xxxxxxxxxxxxxxxxxxxx
export ALERT_EMAIL_ENABLED=true
export ALERT_EMAIL_TO=admin@yourcompany.com
```

```yaml
spring:
  mail:
    host: smtp.sendgrid.net
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
```

#### مثال لـ Mailgun:

```bash
export MAIL_USERNAME=postmaster@yourdomain.com
export MAIL_PASSWORD=your-mailgun-password
```

```yaml
spring:
  mail:
    host: smtp.mailgun.org
    port: 587
```

---

## 🧪 اختبار التكوين

### 1. باستخدام curl (بعد تفعيل الإرسال):

```bash
# تسجيل الدخول والحصول على Token
TOKEN=$(curl -s -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}' | \
  jq -r '.token')

# إنشاء تنبيه (سيتم إرسال إيميل خلال 30 دقيقة)
curl -X POST http://localhost:8081/api/audit/alerts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "alertType": "CRITICAL_STOCK",
    "entityType": "RAW_MATERIAL",
    "entityId": 1,
    "entityName": "Test Material",
    "currentStock": 5,
    "minimumStock": 100,
    "message": "Test alert for email configuration"
  }'
```

### 2. التحقق من Logs:

```bash
# مراقبة السجلات
tail -f app.log | grep -i "email\|alert"

# البحث عن رسائل الإرسال الناجحة
grep "Email sent" app.log

# البحث عن الأخطاء
grep "Failed to send" app.log
```

---

## 🔒 الأمان - Security Best Practices

### ✅ يُنصح به:

1. **استخدم المتغيرات البيئية** (Environment Variables)
   ```bash
   export MAIL_USERNAME=...
   export MAIL_PASSWORD=...
   ```

2. **استخدم App Passwords** بدلاً من كلمة المرور الأساسية (Gmail, Microsoft)

3. **استخدم .env file** (لا تضعه في Git!)
   ```bash
   # .env
   MAIL_USERNAME=your-email@gmail.com
   MAIL_PASSWORD=your-app-password
   ALERT_EMAIL_ENABLED=true
   ```
   
   ثم:
   ```bash
   source .env
   ./start-app.sh
   ```

### ❌ تجنب:

1. **لا تكتب كلمات المرور** مباشرة في `application.yml`
2. **لا تضع ملفات التكوين** التي تحتوي على كلمات مرور في Git
3. **لا تستخدم كلمة المرور الرئيسية** لحساب Gmail

---

## 📊 متغيرات التكوين الكاملة

| المتغير | الوصف | القيمة الافتراضية | مثال |
|---------|-------|-------------------|------|
| `MAIL_USERNAME` | اسم المستخدم للـ SMTP | `your-email@gmail.com` | `admin@company.com` |
| `MAIL_PASSWORD` | كلمة مرور SMTP أو App Password | `your-app-password` | `abcd-efgh-ijkl-mnop` |
| `ALERT_EMAIL_ENABLED` | تفعيل/تعطيل إرسال الإيميلات | `false` | `true` |
| `ALERT_EMAIL_TO` | البريد المستقبل للتنبيهات | `admin@supplychainx.com` | `alerts@company.com` |

---

## 🚀 خطوات التشغيل السريع

### للتطوير (Development):

```bash
# 1. إنشاء App Password من Gmail

# 2. تحديث start-app.sh
nano start-app.sh

# 3. إضافة:
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password
export ALERT_EMAIL_ENABLED=true
export ALERT_EMAIL_TO=your-test-email@gmail.com

# 4. تشغيل التطبيق
chmod +x start-app.sh
./start-app.sh

# 5. إنشاء تنبيه واختبار
./test-audit-module.sh
```

### للإنتاج (Production):

```bash
# استخدم متغيرات بيئية آمنة من النظام
export MAIL_USERNAME=$(vault read -field=username secret/mail)
export MAIL_PASSWORD=$(vault read -field=password secret/mail)
export ALERT_EMAIL_ENABLED=true
export ALERT_EMAIL_TO=alerts@production.com

# أو استخدم Docker Secrets / Kubernetes Secrets
```

---

## 🐛 استكشاف الأخطاء - Troubleshooting

### خطأ: "Authentication failed"

**الحل:**
- تأكد من تفعيل 2FA في Gmail
- استخدم App Password وليس كلمة المرور العادية
- تحقق من صحة username/password

### خطأ: "Could not connect to SMTP host"

**الحل:**
```bash
# اختبر الاتصال بـ SMTP
telnet smtp.gmail.com 587

# تحقق من Firewall
sudo ufw allow out 587/tcp
```

### خطأ: "Email sending is disabled"

**الحل:**
```bash
# تأكد من تفعيل الإرسال
export ALERT_EMAIL_ENABLED=true
```

### الإيميلات لا تُرسل

**التحقق:**
```bash
# 1. تحقق من أن الـ Scheduler يعمل
grep "Starting to send pending alert emails" app.log

# 2. تحقق من وجود تنبيهات معلقة
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8081/api/audit/alerts/unresolved

# 3. تحقق من حالة email_sent
# يجب أن تكون false
```

---

## 📧 نموذج الإيميل المُرسل

```html
⚠️ Alerte de Stock - SupplyChainX
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Détails de l'alerte:
├─ Type: CRITICAL_STOCK
├─ Entité: RAW_MATERIAL
├─ Nom: Acier inoxydable
├─ Stock actuel: 30
├─ Stock minimum: 100
└─ Date: 10/11/2025 14:30:00

Message:
Stock faible détecté pour Acier inoxydable

⚠️ ATTENTION: Cette alerte nécessite une action immédiate!
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

---

## 📝 ملاحظات إضافية

1. **Scheduler يعمل تلقائياً** كل 30 دقيقة
2. **لا يُرسل إيميلات مكررة** للتنبيه نفسه
3. **يواصل العمل** حتى لو فشل إرسال إيميل واحد
4. **يُسجل كل العمليات** في app.log

---

## 🔗 روابط مفيدة

- Gmail App Passwords: https://myaccount.google.com/apppasswords
- Gmail SMTP Settings: https://support.google.com/mail/answer/7126229
- Outlook SMTP: https://support.microsoft.com/en-us/office/pop-imap-and-smtp-settings
- SendGrid Docs: https://docs.sendgrid.com/for-developers/sending-email/integrating-with-the-smtp-api

---

**✅ بعد التكوين الصحيح، النظام سيرسل التنبيهات تلقائياً!** 🚀
