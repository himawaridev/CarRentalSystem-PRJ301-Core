# CarRentalSystem PRJ301 Core

Day la ban tach rieng cua du an CarRentalSystem de thuyet trinh/nop theo de bai PRJ301. Ban nay chi tap trung vao cac chuc nang nam trong de:

- Khach hang tim xe theo loai xe, so cho, thoi gian nhan/tra.
- Khach hang xem thong tin xe va gia tien.
- Khach hang tao hop dong thue xe.
- Khach hang bam thanh toan demo noi bo de xac nhan dat coc va giu xe.
- Mot hop dong co the chon nhieu xe khac nhau.
- Cac xe trong cung hop dong dung chung ngay nhan va ngay tra.
- Moi xe co the thue tu lai hoac co tai xe.
- Nhan vien chap nhan/tu choi/cap nhat don thue xe.
- Nhan vien cap nhat cac moc: dat coc, lay xe, tra xe, thanh toan hoa don.
- Quan ly them/cap nhat xe va gan tai xe.
- Tai xe xem lich lai xe.
- Admin quan tri cac chuc nang lien quan den he thong.

Nhung tinh nang ngoai de bai khong can thuyet trinh:

- Thanh toan that qua cong ben thu ba.
- Google/Facebook OAuth.
- Email verification/reset password.
- Support ticket.
- Deploy Docker/Render/ngrok.
- Hoan coc nang cao.

## Cong nghe

| Thanh phan | Cong nghe |
| --- | --- |
| Backend | Java 17, Jakarta Servlet |
| View | JSP, JSTL |
| Database | Microsoft SQL Server |
| Data access | JDBC, PreparedStatement |
| Server | Apache Tomcat 10.1.x |
| Build | Maven WAR |
| UI | Bootstrap 5, Bootstrap Icons |

## Vai tro va chuc nang

### Customer

- Tim xe theo loai xe, gia, so cho, ngay gio nhan/tra.
- Xem danh sach xe kha dung.
- Chon mot hoac nhieu xe cho cung mot hop dong.
- Chon thue xe tu lai hoac co tai xe.
- Tao hop dong thue xe.
- Thanh toan trong ban Core la thanh toan gia lap noi bo de xac nhan dat coc.
- Khong can cau hinh cong thanh toan ngoai khi demo.
- Xem danh sach hop dong cua minh.

### Staff

- Xem danh sach don/hop dong.
- Chap nhan hoac tu choi don.
- Cap nhat trang thai khi khach lay xe.
- Cap nhat trang thai khi khach tra xe.
- Ghi nhan thanh toan hoa don.

### Manager

- Quan ly danh sach xe.
- Cap nhat thong tin xe.
- Quan ly tai xe.
- Gan tai xe cho lich/hop dong.

### Driver

- Dang nhap vao he thong.
- Xem lich lai xe duoc phan cong.
- Cap nhat trang thai chuyen di neu can.

### Admin

- Quan tri nguoi dung va cac phan quyen chinh.
- Co quyen truy cap cac khu vuc quan tri cua he thong.

## Cau truc thu muc

```text
CarRentalSystem-PRJ301-Core/
+-- config/
|   +-- database-local.example.properties
|   +-- auth-local.example.properties
+-- sql/
|   +-- setup-database.sql
|   +-- check-database.sql
|   +-- upgrade-existing-database.sql
|   +-- enable-sqlserver-tcp.ps1
+-- src/main/java/com/carrental/
|   +-- config/
|   +-- controller/
|   +-- dao/
|   +-- filter/
|   +-- model/
|   +-- service/
+-- src/main/webapp/
|   +-- WEB-INF/views/
|   +-- WEB-INF/includes/
|   +-- css/
|   +-- js/
```

## Chay database

Tao file local:

```text
config/database-local.properties
```

Noi dung mau:

```properties
DB_URL=jdbc:sqlserver://localhost:1433;databaseName=CarRentalDB;encrypt=true;trustServerCertificate=true;loginTimeout=10;
DB_USERNAME=sa
DB_PASSWORD=your_sql_server_password
```

Trong SSMS, chay:

```text
sql/setup-database.sql
```

File nay tao database, bang, tai khoan mau va du lieu xe mau.

Neu can kiem tra:

```text
sql/check-database.sql
```

## Chay du an

Yeu cau:

- JDK 17.
- Apache Tomcat 10.1.x.
- SQL Server.
- Maven hoac NetBeans co Maven support.

Build bang Maven:

```powershell
mvn clean package
```

Deploy file:

```text
target/CarRentalSystem.war
```

vao Tomcat.

URL:

```text
http://localhost:9999/CarRentalSystem/search
```

## Tai khoan mau

```text
admin/admin123
staff01/staff123
manager01/manager123
driver01/driver123
driver02/driver123
customer01/cust123
customer02/cust123
```

## Luong demo de thuyet trinh

1. Customer tim xe theo thoi gian va loai xe.
2. Customer chon nhieu xe.
3. Customer tao hop dong voi cung ngay nhan/tra.
4. Customer bam nut thanh toan gia lap noi bo de hop dong chuyen sang da giu xe.
5. Staff xem hop dong va xu ly don.
6. Manager xem/cap nhat xe va tai xe.
7. Driver xem lich lai.
8. Admin vao trang quan tri.

## Luu y khi thuyet trinh

Chi can thuyet trinh cac chuc nang trong de bai. Phan thanh toan cua ban Core la demo noi bo: bam nut thanh toan de xac nhan dat coc, khong trinh bay nhu cong thanh toan that.
