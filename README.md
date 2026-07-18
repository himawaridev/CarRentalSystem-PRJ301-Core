# CarRentalSystem - PRJ301 Core

Ứng dụng Java Web quản lý cho thuê ô tô, được xây dựng cho Assignment PRJ301.
Dự án sử dụng Jakarta Servlet/JSP theo mô hình MVC, kết nối Microsoft SQL Server
trực tiếp bằng JDBC và đóng gói dưới dạng Maven WAR để chạy trên Tomcat 10.1.

Repository: [himawaridev/CarRentalSystem-PRJ301-Core](https://github.com/himawaridev/CarRentalSystem-PRJ301-Core)

## Mục lục

1. [Chức năng chính](#chức-năng-chính)
2. [Công nghệ và yêu cầu môi trường](#công-nghệ-và-yêu-cầu-môi-trường)
3. [Khởi chạy nhanh sau khi clone](#khởi-chạy-nhanh-sau-khi-clone)
4. [Thiết lập SQL Server chi tiết](#thiết-lập-sql-server-chi-tiết)
5. [Cấu hình kết nối JDBC](#cấu-hình-kết-nối-jdbc)
6. [Chạy bằng NetBeans và Tomcat](#chạy-bằng-netbeans-và-tomcat)
7. [Chạy bằng Maven và Tomcat thủ công](#chạy-bằng-maven-và-tomcat-thủ-công)
8. [Tài khoản mẫu](#tài-khoản-mẫu)
9. [Luồng kiểm thử theo vai trò](#luồng-kiểm-thử-theo-vai-trò)
10. [Quy tắc nghiệp vụ](#quy-tắc-nghiệp-vụ)
11. [Cấu trúc mã nguồn](#cấu-trúc-mã-nguồn)
12. [Xử lý lỗi thường gặp](#xử-lý-lỗi-thường-gặp)

## Chức năng chính

### Khách chưa đăng nhập

- Xem danh mục xe và thông tin chi tiết.
- Lọc xe theo thương hiệu, số chỗ, trạng thái và khoảng giá.
- Tìm xe còn trống theo số chỗ, thương hiệu, khoảng giá và thời gian thuê.
- Đăng ký và đăng nhập tài khoản.

### Khách hàng (`CUSTOMER`)

- Chọn một hoặc nhiều xe trong cùng hợp đồng.
- Chọn thuê có tài xế hoặc tự lái cho từng xe.
- Tạo hợp đồng với cùng thời gian nhận/trả cho tất cả xe.
- Theo dõi trạng thái và xem chi tiết hợp đồng.

### Nhân viên (`STAFF`)

- Chấp nhận hoặc từ chối yêu cầu thuê xe.
- Ghi nhận tiền cọc trực tiếp bằng tiền mặt.
- Cập nhật trạng thái giao xe, nhận xe và thanh toán hóa đơn.
- Thực hiện kết toán khi khách trả xe.

### Quản lý (`MANAGER`)

- Thêm và cập nhật thông tin xe.
- Theo dõi trạng thái đội xe.
- Gán tài xế cho xe có yêu cầu tài xế.

### Tài xế (`DRIVER`)

- Xem lịch lái được phân công.
- Nhận chuyến, nhận xe, bắt đầu và hoàn thành chuyến đi.

### Quản trị viên (`ADMIN`)

- Tạo tài khoản người dùng.
- Cập nhật thông tin và trạng thái tài khoản.
- Cấp hoặc thu hồi vai trò.

## Công nghệ và yêu cầu môi trường

| Thành phần | Phiên bản khuyến nghị |
| --- | --- |
| JDK | 17 |
| Apache NetBeans | 17 trở lên |
| Apache Tomcat | 10.1.x |
| Microsoft SQL Server | 2019/2022 hoặc Developer Edition |
| SQL Server Management Studio | Bản mới nhất tương thích |
| Maven | 3.8 trở lên, hoặc Maven tích hợp trong NetBeans |

Thư viện chính được Maven quản lý trong `pom.xml`:

- Jakarta Servlet API 6.0.
- Jakarta JSP API 3.1.
- JSTL 3.0.
- Microsoft JDBC Driver `mssql-jdbc-13.2.0.jre11`.

> **Quan trọng:** Dự án sử dụng package `jakarta.*`, vì vậy phải dùng Tomcat
> 10.1. Không dùng Tomcat 9 vì Tomcat 9 sử dụng package `javax.*`.

## Khởi chạy nhanh sau khi clone

Thực hiện lần lượt các bước sau:

### Bước 1: Clone repository

```powershell
git clone https://github.com/himawaridev/CarRentalSystem-PRJ301-Core.git
cd CarRentalSystem-PRJ301-Core
```

### Bước 2: Tạo database

1. Mở SQL Server Management Studio (SSMS).
2. Kết nối tới SQL Server bằng tài khoản có quyền tạo database.
3. Mở file `sql/setup-database.sql`.
4. Chạy **toàn bộ** file bằng nút `Execute`.

Script sẽ tạo database `CarRentalCore`, toàn bộ bảng, khóa ngoại, chỉ mục,
trigger, tài khoản mẫu và dữ liệu xe mẫu.

> **Cảnh báo:** `setup-database.sql` sẽ xóa database `CarRentalCore` cũ trước
> khi tạo lại. Hãy sao lưu dữ liệu nếu cần giữ dữ liệu đang có.

### Bước 3: Tạo cấu hình JDBC local

Sao chép file:

```text
src/main/resources/database-local.example.properties
```

thành:

```text
src/main/resources/database-local.properties
```

Trong PowerShell có thể dùng:

```powershell
Copy-Item src/main/resources/database-local.example.properties `
          src/main/resources/database-local.properties
```

Mở file vừa tạo và điền tài khoản SQL Server của máy:

```properties
DB_URL=jdbc:sqlserver://localhost:1433;databaseName=CarRentalCore;encrypt=true;trustServerCertificate=true;loginTimeout=10;
DB_USERNAME=sa
DB_PASSWORD=mat_khau_sql_server_cua_ban
```

Không đưa mật khẩu thật vào file `.example.properties` hoặc bất kỳ file nào
được Git theo dõi.

### Bước 4: Build và chạy

Trong NetBeans:

1. Chọn `File > Open Project` và mở thư mục vừa clone.
2. Đợi Maven tải xong dependencies.
3. Chọn JDK 17 cho project.
4. Thêm Tomcat 10.1 vào NetBeans.
5. Nhấp phải project, chọn `Clean and Build`.
6. Nhấp phải project, chọn `Run`.

Khi chạy thành công, mở:

```text
http://localhost:<PORT>/CarRentalSystem/search
```

Ví dụ nếu Tomcat dùng cổng `8080`:

[http://localhost:8080/CarRentalSystem/search](http://localhost:8080/CarRentalSystem/search)

## Thiết lập SQL Server chi tiết

### Bật chế độ SQL Server Authentication

Nếu dùng tài khoản `sa`:

1. Trong SSMS, nhấp phải server và chọn `Properties`.
2. Chọn `Security`.
3. Chọn `SQL Server and Windows Authentication mode`.
4. Kiểm tra tài khoản `sa` đã được bật và có mật khẩu hợp lệ.
5. Restart dịch vụ SQL Server.

Có thể dùng một SQL login khác thay cho `sa`, miễn tài khoản đó truy cập được
database `CarRentalCore`.

### Bật TCP/IP và cổng 1433

1. Mở `SQL Server Configuration Manager`.
2. Vào `SQL Server Network Configuration > Protocols for MSSQLSERVER`.
3. Bật `TCP/IP`.
4. Mở thuộc tính `TCP/IP > IP Addresses`.
5. Tại `IPAll`, xóa `TCP Dynamic Ports` và đặt `TCP Port` thành `1433`.
6. Restart dịch vụ SQL Server.

Có thể chạy script hỗ trợ bằng PowerShell với quyền Administrator:

```powershell
powershell -ExecutionPolicy Bypass -File sql/enable-sqlserver-tcp.ps1
```

Sau đó vẫn cần restart SQL Server để cấu hình có hiệu lực.

### Kiểm tra database

Sau khi chạy file setup, mở và chạy:

```text
sql/check-database.sql
```

Kết quả cần hiển thị các bảng chính với trạng thái `OK`, dữ liệu xe và tài
khoản mẫu. Nếu database không tồn tại, script sẽ yêu cầu chạy lại
`setup-database.sql`.

## Cấu hình kết nối JDBC

Dự án kết nối trực tiếp bằng JDBC, không sử dụng JPA hoặc Hibernate:

```java
Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
Connection connection = DriverManager.getConnection(url, username, password);
```

`DBContext` hỗ trợ nhiều vị trí cấu hình. Với người mới clone dự án, cách đơn
giản nhất là dùng:

```text
src/main/resources/database-local.properties
```

Các lựa chọn cấu hình được hỗ trợ:

| Cách cấu hình | Ví dụ |
| --- | --- |
| File trong project | `config/database-local.properties` |
| File trong classpath | `src/main/resources/database-local.properties` |
| File ngoài Tomcat | `<CATALINA_BASE>/conf/database-local.properties` |
| Biến môi trường | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` |
| Java system properties | `db.url`, `db.username`, `db.password` |

URL mặc định trỏ tới `localhost:1433` và database `CarRentalCore`. Username và
password không có giá trị mặc định, người chạy dự án bắt buộc phải tự cấu hình.

File `database-local.properties` đã được thêm vào `.gitignore`, vì vậy mật khẩu
không bị đưa lên GitHub. Có thể kiểm tra trước khi commit:

```powershell
git status --short
git check-ignore src/main/resources/database-local.properties
```

Lệnh thứ hai phải trả về đường dẫn của file cấu hình local.

## Chạy bằng NetBeans và Tomcat

### Thêm Tomcat vào NetBeans

1. Mở tab `Services`.
2. Nhấp phải `Servers`, chọn `Add Server`.
3. Chọn `Apache Tomcat or TomEE`.
4. Chọn thư mục cài đặt Tomcat 10.1.
5. Điền tài khoản quản trị Tomcat nếu NetBeans yêu cầu.
6. Hoàn tất và kiểm tra Tomcat xuất hiện trong `Services > Servers`.

### Chọn server cho project

1. Nhấp phải project và chọn `Properties`.
2. Mở mục `Run`.
3. Chọn Tomcat 10.1 vừa thêm.
4. Đặt `Context Path` thành `/CarRentalSystem`.
5. Lưu cấu hình.

### Clean and Build

Nhấp phải project và chọn `Clean and Build`. Khi thành công Maven tạo file:

```text
target/CarRentalSystem.war
```

Nếu vừa thay đổi file cấu hình JDBC, luôn chạy lại `Clean and Build` để file
cấu hình mới được đóng gói vào WAR.

### Run và xác định đúng URL

Nhấp phải project và chọn `Run`. NetBeans sẽ deploy WAR lên Tomcat và thường mở
trình duyệt tự động.

Port phụ thuộc cấu hình Tomcat trên từng máy:

```text
http://localhost:8080/CarRentalSystem/search
http://localhost:9999/CarRentalSystem/search
http://localhost:7000/CarRentalSystem/search
```

Không cố định dùng cổng `7000`; hãy thay `<PORT>` bằng cổng HTTP thực tế của
Tomcat trên máy đang chạy.

Endpoint kiểm tra ứng dụng:

```text
http://localhost:<PORT>/CarRentalSystem/health
```

Kết quả hợp lệ có dạng:

```json
{"status":"ok","app":"CarRentalSystem","time":"..."}
```

Endpoint `/health` chỉ xác nhận ứng dụng đang chạy. Để kiểm tra JDBC, hãy thử
đăng nhập hoặc mở trang tìm kiếm/danh sách xe.

## Chạy bằng Maven và Tomcat thủ công

Build bằng terminal:

```powershell
mvn clean package
```

Sau khi build thành công, sao chép:

```text
target/CarRentalSystem.war
```

vào:

```text
<TOMCAT_HOME>/webapps/CarRentalSystem.war
```

Khởi động Tomcat trên Windows:

```powershell
& "<TOMCAT_HOME>/bin/startup.bat"
```

Dừng Tomcat:

```powershell
& "<TOMCAT_HOME>/bin/shutdown.bat"
```

Nếu terminal báo không nhận diện được lệnh `mvn`, có thể build trực tiếp bằng
`Clean and Build` trong NetBeans hoặc cài Maven và thêm thư mục `bin` của Maven
vào biến môi trường `PATH`.

## Tài khoản mẫu

| Vai trò | Tài khoản | Mật khẩu | Trang chính |
| --- | --- | --- | --- |
| Admin | `admin` | `admin123` | `/admin/dashboard` |
| Staff | `staff01` | `staff123` | `/staff/dashboard` |
| Manager | `manager01` | `manager123` | `/manager/dashboard` |
| Driver | `driver01` | `driver123` | `/driver/schedule` |
| Driver | `driver02` | `driver123` | `/driver/schedule` |
| Customer | `customer01` | `cust123` | `/search` |
| Customer | `customer02` | `cust123` | `/search` |

Các mật khẩu mẫu trong SQL chỉ phục vụ môi trường học tập. Khi đăng nhập thành
công lần đầu, mật khẩu mẫu dạng cũ được nâng cấp sang PBKDF2-HMAC-SHA256. Tài
khoản đăng ký mới được hash trước khi lưu vào database.

Không dùng các mật khẩu mẫu này trong môi trường thật.

## Luồng kiểm thử theo vai trò

Để kiểm tra toàn bộ hệ thống, thực hiện theo thứ tự sau.

### 1. Khách hàng tạo hợp đồng

1. Đăng nhập bằng `customer01 / cust123`.
2. Mở `/search` hoặc `/cars`.
3. Chọn thời gian nhận xe trong tương lai và thời gian trả sau thời gian nhận.
4. Chọn một hoặc nhiều xe.
5. Chọn thuê tài xế cho từng xe nếu cần.
6. Điền địa điểm nhận/trả và tạo hợp đồng.
7. Mở `Hợp đồng của tôi` để kiểm tra trạng thái.

### 2. Nhân viên xử lý đơn

1. Đăng xuất và đăng nhập `staff01 / staff123`.
2. Mở `/staff/dashboard`.
3. Ghi nhận tiền cọc tiền mặt.
4. Chấp nhận hoặc từ chối hợp đồng.
5. Khi xe được trả, mở phần kết toán và ghi nhận thanh toán còn lại.

### 3. Quản lý gán tài xế

1. Đăng nhập `manager01 / manager123`.
2. Mở `/manager/dashboard`.
3. Tìm hợp đồng có xe yêu cầu tài xế.
4. Chọn tài xế còn trống trong khoảng thời gian của hợp đồng.

### 4. Tài xế cập nhật chuyến đi

1. Đăng nhập `driver01 / driver123` hoặc tài xế vừa được gán.
2. Mở `/driver/schedule`.
3. Thực hiện lần lượt các trạng thái nhận chuyến, nhận xe, bắt đầu và hoàn thành.

### 5. Admin quản trị người dùng

1. Đăng nhập `admin / admin123`.
2. Mở `/admin/dashboard`.
3. Tạo tài khoản hoặc cập nhật thông tin người dùng.
4. Cấp vai trò phù hợp và kiểm tra quyền truy cập.

## Quy tắc nghiệp vụ

- Người dùng phải có tài khoản và đăng nhập trước khi tạo hợp đồng.
- Thời gian nhận xe không được nằm trong quá khứ. Việc kiểm tra được thực hiện
  theo phút ở cả giao diện và backend.
- Thời gian trả phải lớn hơn thời gian nhận.
- Thuê dưới hoặc bằng 24 giờ được tính tối thiểu 1 ngày.
- Nếu vượt sang bất kỳ phần nào của chu kỳ 24 giờ tiếp theo, số ngày tính phí
  được làm tròn lên. Ví dụ `24 giờ 01 phút` được tính 2 ngày.
- Một hợp đồng có thể chứa nhiều xe, nhưng tất cả xe phải dùng chung thời gian
  nhận và trả.
- Mỗi xe trong hợp đồng có thể chọn tự lái hoặc có tài xế độc lập.
- Hệ thống kiểm tra trùng lịch ở truy vấn JDBC và trigger trong SQL Server.
- Bản Core ghi nhận đặt cọc và thanh toán trực tiếp bằng tiền mặt, không sử dụng
  cổng thanh toán bên thứ ba.

## Cấu trúc mã nguồn

```text
CarRentalSystem-PRJ301-Core/
|-- pom.xml
|-- README.md
|-- sql/
|   |-- setup-database.sql          # Xóa và tạo lại database Core
|   |-- check-database.sql          # Kiểm tra schema và dữ liệu
|   `-- enable-sqlserver-tcp.ps1    # Hỗ trợ bật TCP/IP
`-- src/main/
    |-- java/com/carrental/
    |   |-- config/                 # DBContext và cấu hình JDBC
    |   |-- controller/             # Servlet điều khiển request/response
    |   |-- dao/                    # Câu lệnh JDBC và transaction
    |   |-- filter/                 # Encoding, session và phân quyền
    |   |-- model/                  # Các model nghiệp vụ
    |   `-- service/                # Hash mật khẩu, kiểm tra thời gian thuê
    |-- resources/
    |   `-- database-local.example.properties
    `-- webapp/
        |-- META-INF/context.xml
        |-- WEB-INF/
        |   |-- includes/           # Header/footer dùng chung
        |   |-- views/              # JSP không truy cập trực tiếp từ URL
        |   `-- web.xml
        |-- css/
        |-- images/
        `-- js/
```

### Mô hình MVC

- **Model:** các lớp trong `model` biểu diễn user, xe, hợp đồng, tài xế và thanh
  toán.
- **View:** JSP trong `WEB-INF/views` chỉ chịu trách nhiệm hiển thị dữ liệu.
- **Controller:** Servlet nhận request, kiểm tra dữ liệu, gọi DAO và chọn JSP.
- **DAO:** sử dụng `Connection`, `PreparedStatement`, `ResultSet` và transaction
  để làm việc với SQL Server.
- **Filter:** quản lý UTF-8, session đăng nhập và phân quyền theo role.

## Đối chiếu yêu cầu PRJ301

| Yêu cầu | Hiện thực |
| --- | --- |
| Java Web theo MVC | Servlet controller, JSP view, model và DAO tách biệt |
| SQL Server qua JDBC | `DBContext` và các lớp DAO |
| Tìm xe theo loại và thời gian | `/search`, `CarSearchServlet`, `CarDAO` |
| Một hợp đồng thuê nhiều xe | `Contracts` và nhiều `Contract_Details` |
| Thuê có hoặc không có tài xế | `RequiresDriver` trên từng chi tiết hợp đồng |
| Nhân viên xử lý đơn và thanh toán | `/staff/dashboard`, `/staff/settlement` |
| Quản lý xe và gán tài xế | `/manager/dashboard` |
| Tài xế xem và cập nhật lịch | `/driver/schedule` |
| Admin quản trị tài khoản và role | `/admin/dashboard` |
| Session và phân quyền | `AuthSessionHelper`, `AuthFilter` |

## Xử lý lỗi thường gặp

### `Login failed for user 'sa'`

- Kiểm tra `DB_USERNAME` và `DB_PASSWORD`.
- Bật `SQL Server and Windows Authentication mode`.
- Bật tài khoản `sa` hoặc dùng SQL login khác.
- Restart SQL Server sau khi thay đổi authentication mode.

### `The TCP/IP connection to the host localhost, port 1433 has failed`

- Kiểm tra dịch vụ SQL Server đang chạy.
- Bật TCP/IP và cổng tĩnh `1433`.
- Restart SQL Server.
- Kiểm tra Windows Firewall nếu kết nối từ máy khác.

### `Cannot open database CarRentalCore`

- Mở SSMS và chạy lại toàn bộ `sql/setup-database.sql`.
- Kiểm tra tên database trong `DB_URL` là `CarRentalCore`.
- Chạy `sql/check-database.sql` để xác nhận.

### `Missing database config DB_USERNAME` hoặc `DB_PASSWORD`

- Tạo `src/main/resources/database-local.properties` từ file mẫu.
- Điền username/password thật.
- Chạy lại `Clean and Build` rồi deploy lại WAR.

### `SQLServerDriver not found`

- Kiểm tra kết nối Internet để Maven tải dependency.
- Trong NetBeans chọn `Reload Project` hoặc tải lại Maven dependencies.
- Chạy `Clean and Build`.
- Không cần chép JDBC JAR thủ công vào Tomcat.

### Deploy thành công nhưng trang báo `404`

- Kiểm tra context path là `/CarRentalSystem`.
- Mở đúng URL `/CarRentalSystem/search`, không mở trực tiếp JSP trong
  `WEB-INF/views`.
- Kiểm tra đúng port của Tomcat.
- Xóa bản deploy cũ trong Tomcat Manager hoặc restart Tomcat rồi deploy lại.

### Tomcat báo `Address already in use`

- Một chương trình khác đang dùng cổng HTTP hiện tại.
- Dừng Tomcat cũ hoặc đổi port trong cấu hình server của NetBeans/Tomcat.
- Sau khi đổi port, cập nhật URL trên trình duyệt.

### Thay đổi CSS/JSP nhưng trình duyệt vẫn hiển thị bản cũ

- Chạy `Clean and Build` và deploy lại.
- Nhấn `Ctrl + F5` để tải lại không dùng cache.
- Nếu chỉnh CSS lớn, tăng tham số phiên bản asset trong `header.jsp`.

### Ảnh xe không hiển thị

- Dữ liệu mẫu sử dụng một số URL ảnh bên ngoài, vì vậy cần kết nối Internet.
- Nếu ảnh ngoài không tải được, giao diện sẽ dùng ảnh placeholder trong project.

## Phạm vi của bản Core

Bản Core tập trung vào yêu cầu PRJ301 và không bao gồm:

- Cổng thanh toán PayOS hoặc thanh toán trực tuyến.
- Google/Facebook OAuth.
- Xác minh email và reset password qua email.
- Support ticket và quy trình hoàn tiền nâng cao.
- Docker, Render hoặc ngrok.

## Lưu ý khi đóng góp mã nguồn

1. Không commit `database-local.properties` hoặc bất kỳ file chứa mật khẩu/key.
2. Không commit thư mục `target/` hay file WAR.
3. Chạy `mvn clean package` hoặc NetBeans `Clean and Build` trước khi commit.
4. Kiểm tra `git status --short` để chắc chắn không có file bí mật.
5. Nếu thay đổi schema, cập nhật đồng thời `setup-database.sql` và README.
