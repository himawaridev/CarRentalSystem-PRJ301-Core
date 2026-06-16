# CarRentalSystem

CarRentalSystem la he thong web quan ly thue xe o to, duoc xay dung bang
Java 17, Jakarta Servlet/JSP va SQL Server theo mo hinh MVC. He thong hien
da ho tro tim xe, xem xe theo dong/thuong hieu, dat nhieu xe trong mot hop
dong, thanh toan PayOS bang ma QR, webhook, doi soat thanh toan, quyet toan
coc va hoan coc khi khach tra xe.

README nay mo ta trang thai hien tai cua du an sau cac phan da refactor va
bo sung tinh nang.

## Muc luc

- Tong quan nghiep vu
- Cong nghe su dung
- Cau truc thu muc
- Chuc nang theo vai tro
- Luong dat xe va thanh toan
- Trang thai hop dong, thanh toan va hoan tien
- Huong dan clone va chay nhanh
- Cau hinh database
- Cau hinh PayOS va ngrok
- Cach chay du an
- Tai khoan mau
- Cac script SQL quan trong
- Kiem thu nhanh
- Loi thuong gap

## Huong dan clone va chay nhanh

Phan nay danh cho may moi clone project tu GitHub.

### 1. Cai dat moi truong

Can co:

- JDK 17.
- Apache Tomcat 10.1.x.
- NetBeans co Java Web/Maven support, hoac Maven trong PATH.
- Microsoft SQL Server.
- SSMS hoac cong cu co the chay file `.sql`.

Nen dat bien moi truong:

```text
JAVA_HOME=C:\Program Files\Java\jdk-17
```

Neu dung NetBeans, them Tomcat trong:

```text
Window > Services > Servers > Add Server > Apache Tomcat or TomEE
```

Chon dung thu muc Tomcat tren may cua ban. Vi du:

```text
F:\Tomcat
```

Trong project:

```text
Right click project > Properties > Run
```

Chon server Tomcat vua them va de context path:

```text
/CarRentalSystem
```

### 2. Cau hinh SQL Server

Project doc cau hinh database tu file local bi ignore:

```text
config/database-local.properties
```

Tao file nay bang cach copy file mau:

```text
config/database-local.example.properties
```

Noi dung can dien:

```properties
DB_URL=jdbc:sqlserver://localhost:1433;databaseName=CarRentalDB;encrypt=true;trustServerCertificate=true;loginTimeout=10;
DB_USERNAME=sa
DB_PASSWORD=your_sql_server_password
```

Co the thay file local bang bien moi truong `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` neu chay tren server/deploy rieng.

Can dam bao SQL Server:

- dang chay,
- bat SQL Server Authentication hoac Mixed Mode,
- login `sa` duoc enable,
- TCP/IP enabled,
- port `1433` mo.

### 3. Tao database va nap du lieu mau

Mo SSMS, ket noi SQL Server, chay:

```text
sql/setup-database.sql
```

File setup da tao schema, tai khoan demo, xe demo, xe test PayOS va `ImageUrl` online tu `4kwallpapers.com`, nen sau khi clone va seed DB,
trang danh sach xe se hien anh neu may co internet.

Neu muon xoa database cu va tao lai sach tu dau, trong SSMS chay truoc:

```sql
USE master;
ALTER DATABASE CarRentalDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
DROP DATABASE CarRentalDB;
```

Sau do chay lai:

```text
sql/setup-database.sql
```

Co the chay script kiem tra:

```text
sql/check-database.sql
```

### 4. Build va chay

Bang NetBeans:

```text
Right click project > Clean and Build
Right click project > Run
```

Bang Maven:

```powershell
mvn clean package
```

Sau do deploy file:

```text
target/CarRentalSystem.war
```

vao Tomcat 10.1.x.

URL mac dinh:

```text
http://localhost:9999/CarRentalSystem/search
```

Neu Tomcat dung port khac, thay `9999` bang port cua may do.

### 5. Tai khoan dang nhap mau

```text
admin/admin123
staff01/staff123
manager01/manager123
driver01/driver123
customer01/cust123
```

Mat khau demo dang o dinh dang legacy trong file SQL. Lan dang nhap thanh cong dau tien se tu dong nang cap sang hash PBKDF2 trong database.

### 6. Cau hinh email/OAuth neu can test dang ky va reset password

Tao file local, khong commit:

```text
config/auth-local.properties
```

Co the copy tu:

```text
config/auth-local.example.properties
```

Toi thieu de test local khong can gui mail that:

```properties
APP_BASE_URL=http://localhost:9999/CarRentalSystem
AUTH_DEV_MODE=true
```

Khi `AUTH_DEV_MODE=true`, ma xac minh email/reset password se hien tren man hinh va log server. Khi dung that, cau hinh SMTP va tat dev mode:

```properties
AUTH_DEV_MODE=false
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your_email@gmail.com
SMTP_PASSWORD=your_email_app_password
SMTP_FROM=your_email@gmail.com
SMTP_STARTTLS=true
SMTP_SSL=false
```

Google/Facebook login chi hien nut khi da cau hinh client id/secret:

```properties
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
FACEBOOK_CLIENT_ID=your_facebook_app_id
FACEBOOK_CLIENT_SECRET=your_facebook_app_secret
FACEBOOK_GRAPH_VERSION=v25.0
```

Callback URL can khai bao tren provider:

```text
http://localhost:9999/CarRentalSystem/oauth/google/callback
http://localhost:9999/CarRentalSystem/oauth/facebook/callback
```

Neu dung ngrok, doi `APP_BASE_URL` sang domain ngrok va cap nhat callback URL tuong ung.

### 7. Cau hinh thanh toan PayOS neu can test QR that

Tao file local, khong commit:

```text
config/payment-local.properties
```

Noi dung mau:

```properties
PAYOS_CLIENT_ID=your_client_id
PAYOS_API_KEY=your_api_key
PAYOS_CHECKSUM_KEY=your_checksum_key
APP_BASE_URL=http://localhost:9999/CarRentalSystem
```

Neu khong cau hinh PayOS, cac trang public, dang nhap, quan ly xe, tim xe van chay;
phan tao QR thanh toan that se bao thieu cau hinh.

## Tong quan nghiep vu

He thong phuc vu quy trinh cho thue xe:

1. Khach hang tim xe theo thuong hieu, so cho, khoang gia va thoi gian nhan/tra.
2. Khach hang co the chon mot hoac nhieu xe cho cung mot hop dong.
3. Tat ca xe trong cung mot hop dong bat buoc dung chung ngay gio nhan xe va tra xe.
4. Moi xe co the duoc chon tu lai hoac co tai xe.
5. Khach hang bat buoc nhap dia chi nhan xe va dia chi tra xe.
6. He thong tao hop dong o trang thai cho thanh toan.
7. Khach hang thanh toan tien coc hoac thanh toan toan bo bang QR PayOS.
8. Khi PayOS xac nhan thanh cong, hop dong duoc giu xe.
9. Nhan vien xac nhan, ban giao xe, ghi nhan tra xe va quyet toan.
10. Neu con tien phai thu, nhan vien thu them.
11. Neu can hoan coc, nhan vien chon phuong thuc hoan tien va hoan tat quyet toan.

## Cong nghe su dung

| Thanh phan | Cong nghe |
| --- | --- |
| Ngon ngu | Java 17 |
| Web framework | Jakarta Servlet 6, JSP, JSTL |
| Server | Apache Tomcat 10.1.x |
| Build | Maven WAR project |
| Database | Microsoft SQL Server |
| Data access | JDBC, PreparedStatement, transaction thu cong |
| Frontend | JSP, Bootstrap 5, Bootstrap Icons, CSS rieng |
| Slider anh | SwiperJS CDN, co fallback JS noi bo |
| Thanh toan | PayOS payment link, VietQR/QR dynamic, webhook |
| QR local | ZXing |
| Public tunnel dev | ngrok |

## Cau truc thu muc

```text
CarRentalSystem/
+-- config/
|   +-- auth-local.example.properties
|   +-- database-local.example.properties
+-- docs/
|   +-- CarRentalSystem_Functional_Requirements.docx
|   +-- build_requirements_doc.py
|   +-- payment-webhook-refund-design.md
+-- sql/
|   +-- setup-database.sql
|   +-- upgrade-existing-database.sql
|   +-- check-database.sql
|   +-- enable-sqlserver-tcp.ps1
+-- src/main/java/com/carrental/
|   +-- config/
|   |   +-- DBContext.java
|   +-- controller/
|   |   +-- AdminDashboardServlet.java
|   |   +-- BookingServlet.java
|   |   +-- CarCatalogServlet.java
|   |   +-- CarSearchServlet.java
|   |   +-- ContractDetailServlet.java
|   |   +-- ContractProcessServlet.java
|   |   +-- CustomerContractsServlet.java
|   |   +-- DriverScheduleServlet.java
|   |   +-- LoginServlet.java
|   |   +-- LogoutServlet.java
|   |   +-- ManagerDashboardServlet.java
|   |   +-- PaymentPendingServlet.java
|   |   +-- PaymentQrServlet.java
|   |   +-- PaymentStatusServlet.java
|   |   +-- PayOsWebhookServlet.java
|   |   +-- RegisterServlet.java
|   |   +-- SettlementServlet.java
|   |   +-- StaffDashboardServlet.java
|   +-- dao/
|   |   +-- CarDAO.java
|   |   +-- ContractDAO.java
|   |   +-- DriverDAO.java
|   |   +-- PaymentDAO.java
|   |   +-- UserDAO.java
|   +-- filter/
|   |   +-- AuthFilter.java
|   |   +-- CharacterEncodingFilter.java
|   +-- model/
|   |   +-- Car.java
|   |   +-- Contract.java
|   |   +-- ContractStatus.java
|   |   +-- Payment.java
|   |   +-- PaymentMode.java
|   |   +-- PaymentStatus.java
|   |   +-- PaymentTransaction.java
|   |   +-- PaymentType.java
|   |   +-- PaymentWebhookResult.java
|   |   +-- Refund.java
|   |   +-- RefundMethod.java
|   |   +-- SettlementResult.java
|   +-- service/
|       +-- CheckoutService.java
|       +-- PaymentLinkRequest.java
|       +-- PaymentLinkResponse.java
|       +-- PaymentLinkStatusResponse.java
|       +-- PayOsGateway.java
+-- src/main/webapp/
|   +-- css/style.css
|   +-- images/car-placeholder.svg
|   +-- META-INF/context.xml
|   +-- WEB-INF/
|       +-- includes/header.jsp
|       +-- includes/footer.jsp
|       +-- views/
|       +-- web.xml
+-- pom.xml
+-- README.md
```

## Chuc nang theo vai tro

### Khach hang

- Dang ky va dang nhap.
- Tim xe tren trang chu theo:
  - thuong hieu,
  - so cho,
  - khoang gia tuy chon,
  - ngay gio nhan xe,
  - ngay gio tra xe.
- Xem danh sach xe tren trang catalog.
- Xem so luong xe con kha dung theo dong xe/thong tin xe.
- Dat mot xe hoac nhieu xe trong cung mot hop dong.
- Chon co tai xe hoac tu lai cho tung xe.
- Nhap dia chi nhan xe va dia chi tra xe bat buoc.
- Chon hinh thuc thanh toan:
  - thanh toan tien coc,
  - thanh toan toan bo.
- Quet QR PayOS de thanh toan.
- Theo doi trang thai thanh toan tai trang cho thanh toan.
- Xem danh sach hop dong cua minh.
- Huy hop dong khi hop dong chua qua cac trang thai van hanh khong cho phep huy.

### Nhan vien

- Xem dashboard hop dong.
- Loc hop dong theo trang thai.
- Xac nhan hop dong da giu xe.
- Cap nhat trang thai:
  - da xac nhan,
  - da nhan xe,
  - da tra xe,
  - dang quyet toan,
  - hoan tat.
- Mo trang quyet toan khi khach tra xe.
- Nhap phi phat sinh neu co.
- Thu tien con thieu neu khach chua thanh toan du.
- Xu ly hoan coc bang nhieu phuong thuc:
  - hoan qua cong thanh toan,
  - hoan tien mat tai quay,
  - chuyen khoan thu cong,
  - ghi co vi noi bo neu can mo rong.

### Quan ly

- Quan ly danh sach xe.
- Them, sua thong tin xe.
- Quan ly trang thai xe.
- Xem va dieu phoi hop dong can tai xe.
- Phan cong tai xe cho hop dong co yeu cau tai xe.
- Theo doi lich va trang thai phan cong.

### Tai xe

- Dang nhap vao khu vuc tai xe.
- Xem lich xe duoc phan cong.
- Theo doi thong tin thoi gian, xe va hop dong lien quan.

### Quan tri vien

- Quan ly tai khoan nguoi dung.
- Kich hoat, khoa tai khoan.
- Quan ly cac vai tro he thong.
- Co quyen truy cap cac man hinh quan tri/chuc nang cao.

## Luong dat xe va thanh toan

### 1. Tim xe

Nguoi dung vao:

```text
/search
```

Co the tim theo:

- thuong hieu,
- so cho,
- khoang gia,
- thoi gian nhan xe,
- thoi gian tra xe.

Ket qua tim kiem chi nen tra ve cac xe phu hop va con kha dung trong khoang thoi gian da chon.

### 2. Xem xe

Nguoi dung vao:

```text
/cars
```

Trang nay hien thi xe theo nhom thay vi liet ke dai tat ca bien so. Khi co nhieu xe cung thuong hieu/dong xe, UI uu tien hien thi so luong con kha dung va hinh anh dai dien.

Neu du lieu anh rong hoac URL anh loi, he thong tu dung:

```text
/images/car-placeholder.svg
```

### 3. Chon nhieu xe trong mot hop dong

Quy tac:

- Mot hop dong co the gom nhieu xe.
- Cac xe trong cung mot hop dong bat buoc co cung thoi gian nhan va tra.
- Neu khach muon thue cac xe o thoi diem khac nhau, phai tao hop dong khac.
- He thong se chon ngau nhien xe vat ly con trong kho doi voi dong xe co nhieu bien so.
- Khong can hien thi tat ca bien so cho khach neu so luong xe lon.

### 4. Bat buoc nhap dia chi

Truoc khi tao hop dong, backend validate:

- dia chi nhan xe khong duoc rong,
- dia chi tra xe khong duoc rong,
- chuoi dia chi duoc trim de bo khoang trang thua.

Neu thieu dia chi, he thong tra loi loi va khong tao hop dong, contract detail hay payment.

### 5. Tao hop dong

Khi dat xe hop le:

- tao ban ghi `Contracts`,
- tao cac dong `Contract_Details`,
- tao `Payment_Transactions`,
- tao cac dong hach toan trong `Payments`,
- tao payment link/QR PayOS.

Hop dong ban dau o trang thai:

```text
PENDING_PAYMENT
```

### 6. Kich ban thanh toan coc

Khach chi thanh toan tien coc de giu xe.

He thong ghi nhan:

- `PaymentType = DEPOSIT`,
- `PaymentStatus = PENDING` khi moi tao,
- `PaymentStatus = PAID` khi PayOS xac nhan.

Sau khi coc thanh cong:

- hop dong chuyen sang `RESERVED`,
- tien thue con lai se duoc thu sau bang tien mat/chuyen khoan.

### 7. Kich ban thanh toan toan bo

Khach thanh toan:

- tien coc,
- tien thue,
- phi tai xe neu co.

He thong van tach rieng cac dong hach toan:

- `DEPOSIT`,
- `RENTAL_PREPAID`,
- `DRIVER_FEE_PREPAID`.

Muc dich la de quyet toan dung:

- tien thue va phi tai xe la doanh thu,
- tien coc co the hoan lai sau khi tru phat sinh.

### 8. PayOS webhook

PayOS goi ve:

```text
/payment/webhook/payos
```

Backend thuc hien:

- doc payload JSON,
- verify signature bang checksum key,
- kiem tra giao dich thanh cong,
- xu ly idempotency de webhook lap lai khong cong tien hai lan,
- cap nhat transaction va payment lines sang `PAID`,
- cap nhat hop dong sang `RESERVED` neu coc da thanh toan.

### 9. Doi soat fallback tu trang pending

Thuc te dev local co the gap truong hop:

- webhook bi miss,
- ngrok doi URL,
- PayOS goi webhook cham,
- trinh duyet khach quay ve truoc khi webhook toi.

Vi vay `/payment/status` co co che du phong:

- neu DB van `PENDING`,
- backend goi PayOS API lay thong tin payment link bang `orderCode`,
- neu PayOS tra `PAID`, backend cap nhat DB nhu webhook,
- trang pending hien `PAID` va tu chuyen ve `/my-contracts`.

## Trang thai hop dong

| Trang thai | Y nghia |
| --- | --- |
| `PENDING_PAYMENT` | Hop dong da tao, dang cho thanh toan coc/toan bo |
| `PAYMENT_EXPIRED` | Link thanh toan het han |
| `RESERVED` | Da thanh toan coc, xe da duoc giu |
| `CONFIRMED` | Nhan vien da xac nhan hop dong |
| `CANCELLED` | Hop dong da bi huy |
| `CAR_PICKED_UP` | Khach da nhan xe |
| `CAR_RETURNED` | Khach da tra xe |
| `SETTLEMENT_PENDING` | Dang quyet toan coc/phat sinh |
| `COMPLETED` | Hop dong da hoan tat |

## Trang thai thanh toan

| Trang thai | Y nghia |
| --- | --- |
| `PENDING` | Dang cho thanh toan |
| `PAID` | Da thanh toan thanh cong |
| `FAILED` | Thanh toan that bai |
| `EXPIRED` | Het han thanh toan |
| `REFUND_PENDING` | Dang cho hoan tien |
| `REFUNDED` | Da hoan tien day du |
| `PARTIALLY_REFUNDED` | Da hoan mot phan |

## Loai hach toan thanh toan

| PaymentType | Y nghia |
| --- | --- |
| `DEPOSIT` | Tien coc giu xe |
| `RENTAL_PREPAID` | Tien thue tra truoc |
| `DRIVER_FEE_PREPAID` | Phi tai xe tra truoc |
| `RENTAL_BALANCE` | Tien thue con lai thu sau |
| `EXTRA_CHARGE` | Phi phat sinh |
| `REFUND` | Dong ghi nhan hoan tien |

## Phuong thuc hoan tien

| RefundMethod | Y nghia |
| --- | --- |
| `GATEWAY_REFUND` | Hoan tien qua cong thanh toan neu cong ho tro |
| `CASH_AT_COUNTER` | Hoan tien mat tai quay |
| `MANUAL_BANK_TRANSFER` | Ke toan chuyen khoan thu cong |
| `WALLET_CREDIT` | Ghi co vao vi noi bo/diem tich luy |

## Quyet toan coc khi tra xe

Khi khach tra xe, nhan vien mo trang:

```text
/staff/settlement?contractId={id}
```

He thong tinh:

- tong tien coc da thanh toan,
- tien thue/phi tai xe da tra,
- phi phat sinh,
- so tien can thu them,
- so tien can hoan lai.

Cong thuc tong quat:

```text
refundAmount = max(depositPaid - deductionAmount, 0)
amountToCollect = max(extraCharge + rentalBalance - depositPaid, 0)
```

Neu coc du de tru phi phat sinh:

- he thong tao yeu cau hoan tien phan con lai.

Neu coc khong du:

- he thong hien so tien can thu them.

## Cau hinh database

File ket noi:

```text
src/main/java/com/carrental/config/DBContext.java
```

File nay khong chua username/password that. Khi chay local, tao file:

```text
config/database-local.properties
```

Tu file mau:

```text
config/database-local.example.properties
```

Noi dung:

```properties
DB_URL=jdbc:sqlserver://localhost:1433;databaseName=CarRentalDB;encrypt=true;trustServerCertificate=true;loginTimeout=10;
DB_USERNAME=sa
DB_PASSWORD=your_sql_server_password
```

`config/database-local.properties` da nam trong `.gitignore`, khong commit len Git.
Khi deploy server, co the dung bien moi truong `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
hoac Java system property `db.url`, `db.username`, `db.password`.

Yeu cau SQL Server:

- SQL Server dang chay,
- TCP/IP enabled,
- port 1433 mo,
- database ten `CarRentalDB`,
- user co quyen tao bang, sua bang va insert/update data.

## Tao database va seed du lieu

Tat ca huong dan SQL nam trong README chung nay. Thu muc `sql/` chi giu script can chay.

Neu tao database moi hoac vua xoa `CarRentalDB`:

1. Mo SSMS.
2. Chay:

```text
sql/setup-database.sql
```

File nay tao schema moi nhat, tai khoan demo, xe demo, xe PayOS test va anh online.
Schema nay da bao gom hash password, bang pending email verification, bang reset password va OAuth provider mapping.

Neu database cu da ton tai va can nang cap len schema moi:

```text
sql/upgrade-existing-database.sql
```

Neu can xoa database cu de tao lai sach tu dau:

```sql
USE master;
ALTER DATABASE CarRentalDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
DROP DATABASE CarRentalDB;
```

Sau do chay `sql/setup-database.sql`.

Script ho tro kiem tra:

```text
sql/check-database.sql
```

Neu vua pull code moi tren database cu, chay `sql/upgrade-existing-database.sql` de them cac cot/bang auth moi truoc khi dang nhap.

## Cau hinh dang nhap, xac minh email va reset password

Project ho tro:

- Hash mat khau bang PBKDF2. Tai khoan legacy/demo tu nang cap hash sau lan login thanh cong dau tien.
- Dang ky 2 buoc: gui ma 6 so toi email, chi tao user that sau khi nhap dung ma.
- Reset password bang ma 6 so gui qua email.
- Dang nhap Google/Facebook bang OAuth 2.0 khi da co client id/secret.

File cau hinh local:

```text
config/auth-local.properties
```

File nay bi ignore va khong nen commit vi co the chua SMTP password, Google client secret, Facebook app secret.

## Cau hinh PayOS

File cau hinh local:

```text
config/payment-local.properties
```

File nay bi ignore va khong nen commit vi chua key rieng cua kenh thanh toan.

Mau cau hinh:

```properties
PAYOS_CLIENT_ID=your_client_id
PAYOS_API_KEY=your_api_key
PAYOS_CHECKSUM_KEY=your_checksum_key
APP_BASE_URL=http://localhost:9999/CarRentalSystem
```

PayOS trong du an chi dung cho luong thu tien bang QR:

- Khach thanh toan coc hoac thanh toan toan bo khi dat xe.
- Neu khach moi thanh toan coc, den buoc quyet toan nhan vien tao QR PayOS de thu not tien thue/tai xe con lai.
- Nhan vien bam kiem tra giao dich PayOS de doi soat. Khi PayOS bao `PAID`, he thong ghi nhan dong `RENTAL_BALANCE`.
- Sau khi thu du tien thue/tai xe, nhan vien tao QR hoan coc thu cong cho tai khoan ngan hang cua khach, tu quet QR chuyen tien va nhap ma giao dich ngan hang de hoan tat.

Du an khong dung PayOS Chi de tu dong chuyen tien hoan coc, vi Kenh Chi phu thuoc tai khoan dich/payOS/Napas va co the tu choi tai khoan hop le khi chuyen thu cong. Luong hoan coc mac dinh la QR thu cong de on dinh hon khi clone ve chay demo.

Khi dung ngrok:

```properties
APP_BASE_URL=https://your-ngrok-domain.ngrok-free.dev/CarRentalSystem
```

Khong them dau `/` o cuoi `APP_BASE_URL`.

## Cau hinh webhook PayOS

Webhook URL tren PayOS phai la:

```text
https://your-ngrok-domain.ngrok-free.dev/CarRentalSystem/payment/webhook/payos
```

Vi du:

```text
https://your-domain.ngrok-free.dev/CarRentalSystem/payment/webhook/payos
```

Sau khi sua webhook tren PayOS, nen tao giao dich test moi de kiem tra.

## Chay ngrok cho moi lan test PayOS

Neu Tomcat dang chay port `9999`, mo PowerShell tai thu muc du an:

```powershell
cd D:\FPT\PRJ301\ASG\CarRentalSystem
```

Neu da co domain co dinh:

```powershell
.\tools\ngrok\ngrok.exe http --domain=your-domain.ngrok-free.dev 9999
```

Neu khong co domain co dinh:

```powershell
.\tools\ngrok\ngrok.exe http 9999
```

Sau do:

1. Copy URL HTTPS cua ngrok.
2. Cap nhat `APP_BASE_URL`.
3. Cap nhat webhook URL tren PayOS.
4. Reload/restart app Tomcat neu vua sua config.
5. Test:

```text
https://your-ngrok-domain.ngrok-free.dev/CarRentalSystem
```

Trang theo doi request ngrok:

```text
http://127.0.0.1:4040
```

## Cach chay du an

### Cach 1: Chay bang NetBeans

1. Mo NetBeans.
2. Open Project va chon thu muc `CarRentalSystem`.
3. Cau hinh server Apache Tomcat 10.1.x.
4. Clean and Build.
5. Run.
6. Mo:

```text
http://localhost:9999/CarRentalSystem/search
```

Neu Tomcat cua ban dung port khac, thay `9999` bang port tuong ung.

### Cach 2: Build bang Maven

Neu may co Maven trong PATH:

```powershell
mvn clean package
```

File WAR sinh ra:

```text
target/CarRentalSystem.war
```

Deploy WAR vao Tomcat hoac cau hinh context tro toi:

```text
target/CarRentalSystem
```

### Cach 3: Reload context Tomcat khi dang dung exploded deployment

Neu Tomcat dang doc context:

```text
D:\ApacheTomcat\conf\Catalina\localhost\CarRentalSystem.xml
```

va `docBase` tro toi:

```text
D:\FPT\PRJ301\ASG\CarRentalSystem\target\CarRentalSystem
```

thi sau khi build/copy class moi, co the reload context bang cach cham vao file descriptor hoac restart Tomcat.

## Tai khoan mau

| Vai tro | Username | Password | Ghi chu |
| --- | --- | --- | --- |
| Admin | `admin` | `admin123` | Quan tri he thong |
| Staff | `staff01` | `staff123` | Xu ly hop dong va quyet toan |
| Manager | `manager01` | `manager123` | Quan ly xe va phan cong tai xe |
| Driver | `driver01` | `driver123` | Xem lich lai xe |
| Driver | `driver02` | `driver123` | Xem lich lai xe |
| Customer | `customer01` | `cust123` | Dat xe va thanh toan |
| Customer | `customer02` | `cust123` | Dat xe va thanh toan |

## Cac URL chinh

| URL | Chuc nang |
| --- | --- |
| `/search` | Trang chu va tim xe |
| `/cars` | Danh sach xe/catalog |
| `/book` | Tao hop dong/dat xe |
| `/payment/pending?ref=...` | Trang cho thanh toan QR |
| `/payment/status?ref=...` | API polling trang thai thanh toan |
| `/payment/qr?ref=...` | QR image cho giao dich |
| `/payment/webhook/payos` | Webhook PayOS |
| `/my-contracts` | Hop dong cua khach hang |
| `/contract-detail?id=...` | Chi tiet hop dong |
| `/staff/dashboard` | Dashboard nhan vien |
| `/staff/settlement?contractId=...` | Quyet toan/hoan coc |
| `/manager/dashboard` | Dashboard quan ly |
| `/driver/schedule` | Lich tai xe |
| `/admin/dashboard` | Dashboard admin |

## Phan quyen

He thong dung `AuthFilter` de bao ve cac route.

| Khu vuc | Quyen truy cap |
| --- | --- |
| `/search`, `/cars`, `/login`, `/register` | Public |
| `/book`, `/my-contracts`, `/contract-detail` | Customer da dang nhap |
| `/payment/pending`, `/payment/status`, `/payment/qr` | Customer so huu giao dich |
| `/payment/webhook/payos` | Public endpoint, bao ve bang signature |
| `/staff/*` | Staff, Manager, Admin |
| `/manager/*` | Manager, Admin |
| `/driver/*` | Driver |
| `/admin/*` | Admin |

## UI hien tai

- Trang chu co khoi tim kiem theo filter moi.
- Trang chu co khu xe noi bat theo thuong hieu.
- Car card co fallback image.
- Neu mot thuong hieu co nhieu anh, card dung slider tu dong.
- Slider:
  - autoplay moi 3 giay,
  - lap vo han,
  - ho tro swipe tren mobile,
  - object-fit cover de giu layout on dinh.
- Trang catalog khong liet ke tat ca bien so khi so luong xe lon, tranh lam UI roi.

## Tai lieu nghiep vu

Tai lieu chuc nang chi tiet nam trong:

```text
docs/CarRentalSystem_Functional_Requirements.docx
docs/payment-webhook-refund-design.md
```

## Kiem thu nhanh sau khi chay

### Kiem tra trang public

```text
http://localhost:9999/CarRentalSystem/search
http://localhost:9999/CarRentalSystem/cars
```

### Kiem tra thanh toan PayOS

1. Dang nhap `customer01/cust123`.
2. Dat mot xe gia test nho.
3. Chon thanh toan coc hoac thanh toan toan bo.
4. Trang `/payment/pending` phai hien QR.
5. Sau khi thanh toan thanh cong:
   - webhook PayOS cap nhat DB, hoac
   - `/payment/status` tu doi soat PayOS va cap nhat DB.
6. Trang pending hien `PAID`.
7. Trang tu chuyen sang `/my-contracts`.

### Kiem tra quyet toan

1. Dang nhap staff.
2. Dua hop dong qua cac trang thai van hanh den `CAR_RETURNED`.
3. Mo trang settlement.
4. Nhap phi phat sinh neu co.
5. Chon phuong thuc hoan coc hoac thu them.
6. Hoan tat quyet toan.

## Loi thuong gap

### Trang thanh toan bao chua cau hinh PayOS

Kiem tra:

- `config/payment-local.properties` co ton tai khong,
- da dien `PAYOS_CLIENT_ID`, `PAYOS_API_KEY`, `PAYOS_CHECKSUM_KEY` chua,
- `APP_BASE_URL` dung domain hien tai chua,
- Tomcat da reload sau khi sua config chua.

### Webhook PayOS bao thanh cong nhung app van pending

Kiem tra:

- URL webhook tren PayOS co dung `/CarRentalSystem/payment/webhook/payos` khong,
- ngrok con dang chay khong,
- `APP_BASE_URL` co trung domain ngrok khong,
- trang `http://127.0.0.1:4040` co thay request webhook khong.

Neu webhook bi miss, `/payment/status` se tu doi soat PayOS va cap nhat khi PayOS tra `PAID`.

### Loi CHECK constraint Contracts.Status

Database dang co constraint status cu. Can chay migration/refactor SQL tuong ung de bo sung cac trang thai moi:

```text
PENDING_PAYMENT
PAYMENT_EXPIRED
RESERVED
CONFIRMED
CANCELLED
CAR_PICKED_UP
CAR_RETURNED
SETTLEMENT_PENDING
COMPLETED
```

### Khong vao duoc database

Kiem tra:

- SQL Server dang chay,
- TCP/IP port 1433 da enable,
- database `CarRentalDB` ton tai,
- `config/database-local.properties` da co `DB_USERNAME` va `DB_PASSWORD` dung,
- firewall khong chan SQL Server.

### Anh xe bi vo layout

Kiem tra:

- `ImageUrl` trong database co hop le khong,
- neu rong, he thong tu dung `car-placeholder.svg`,
- neu URL ngoai bi loi, su kien `onerror` se doi sang placeholder.

## Ghi chu bao mat

Khong commit cac file/chia se thong tin sau:

- `config/database-local.properties`,
- `config/payment-local.properties`,
- `config/auth-local.properties`,
- PayOS client id/api key/checksum key that,
- SMTP password, Google client secret, Facebook app secret,
- ngrok authtoken,
- file exe/zip/log trong `tools/ngrok`,
- password database production.

## Trang thai hien tai cua du an

Du an hien co cac nhom tinh nang lon:

- MVC Jakarta Servlet/JSP co phan quyen theo role.
- Tim xe nang cao theo thuong hieu, so cho, gia va thoi gian.
- Catalog xe co grouping va so luong kha dung.
- Dat nhieu xe trong mot hop dong.
- Validate dia chi nhan/tra xe.
- PayOS QR dynamic.
- Webhook verify signature va idempotency.
- Doi soat PayOS fallback khi webhook cham/miss.
- Hach toan thanh toan tach dong theo loai tien.
- Quyet toan coc va hoan tien da phuong thuc.
- UI card xe co slider anh va fallback image.

## License

Du an phuc vu mon PRJ301 - FPT University.
