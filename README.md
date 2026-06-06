# 🚗 Car Rental System

Hệ thống web cho thuê xe ô tô trực tuyến, được xây dựng bằng **Java Servlet/JSP** theo mô hình **MVC**, hỗ trợ quản lý toàn bộ quy trình từ tìm kiếm xe, đặt xe, ký hợp đồng đến thanh toán.

## ✨ Tính năng chính

### 👤 Khách hàng (Customer)
- Tìm kiếm xe theo **loại xe** (4/5/7 chỗ) và **khoảng thời gian** thuê
- Đặt **nhiều xe cùng lúc** trong một hợp đồng
- Tùy chọn **thuê xe có tài xế** hoặc tự lái
- Theo dõi trạng thái hợp đồng real-time

### 👨‍💼 Nhân viên (Staff)
- Duyệt / Từ chối đơn đặt xe
- Quản lý quy trình hợp đồng: `Chờ duyệt → Đã duyệt → Đã đặt cọc → Đã nhận xe → Đã trả xe → Hoàn tất`
- Lọc hợp đồng theo trạng thái

### 🔧 Quản lý (Manager)
- Thêm / Sửa / Xóa xe trong hệ thống
- Phân công tài xế cho các hợp đồng cần tài xế
- Quản lý đội xe và thông tin chi tiết

### 🚘 Tài xế (Driver)
- Xem lịch lái xe được phân công
- Cập nhật trạng thái nhiệm vụ

### 🛡️ Quản trị viên (Admin)
- Quản lý tất cả tài khoản người dùng
- Tạo tài khoản mới với nhiều vai trò
- Khóa / Mở khóa tài khoản
- Phân quyền linh hoạt

## 🏗️ Kiến trúc

```
MVC Architecture
├── Model      → Java POJOs (9 entity classes)
├── View       → JSP + JSTL + Bootstrap 5
├── Controller → Jakarta Servlets (12 servlets)
└── DAO        → JDBC with PreparedStatement (4 DAO classes)
```

```
CarRentalSystem/
├── sql/
│   ├── database-schema.sql          # Schema + Triggers
│   └── test-data.sql                # Dữ liệu mẫu
├── src/main/java/com/carrental/
│   ├── config/
│   │   └── DBContext.java           # Kết nối SQL Server
│   ├── controller/
│   │   ├── LoginServlet.java        # Đăng nhập
│   │   ├── RegisterServlet.java     # Đăng ký
│   │   ├── LogoutServlet.java       # Đăng xuất
│   │   ├── CarSearchServlet.java    # Tìm kiếm xe
│   │   ├── BookingServlet.java      # Đặt xe
│   │   ├── CustomerContractsServlet # Hợp đồng khách hàng
│   │   ├── StaffDashboardServlet    # Dashboard nhân viên
│   │   ├── ContractProcessServlet   # Xử lý trạng thái HD
│   │   ├── ContractDetailServlet    # Chi tiết hợp đồng
│   │   ├── ManagerDashboardServlet  # Dashboard quản lý
│   │   ├── DriverScheduleServlet    # Lịch tài xế
│   │   └── AdminDashboardServlet    # Dashboard admin
│   ├── dao/
│   │   ├── UserDAO.java             # CRUD Users + Auth
│   │   ├── CarDAO.java              # CRUD Cars + Search
│   │   ├── ContractDAO.java         # Contracts + Details
│   │   └── DriverDAO.java           # Drivers + Assignments
│   ├── filter/
│   │   ├── AuthFilter.java          # Phân quyền truy cập
│   │   └── CharacterEncodingFilter  # Encoding UTF-8
│   └── model/
│       ├── User.java, Role.java
│       ├── Car.java, CarType.java
│       ├── Contract.java, ContractDetail.java
│       ├── Customer.java, Driver.java
│       └── DriverAssignment.java
├── src/main/webapp/
│   ├── css/style.css                # Light theme CSS
│   ├── WEB-INF/
│   │   ├── web.xml                  # Servlet 6.0 config
│   │   ├── includes/
│   │   │   ├── header.jsp           # Navbar + Head
│   │   │   └── footer.jsp           # Scripts
│   │   └── views/                   # 11 JSP pages
│   └── META-INF/context.xml
├── pom.xml                          # Maven dependencies
└── nb-configuration.xml             # NetBeans config
```

## 🛠️ Công nghệ sử dụng

| Thành phần | Công nghệ | Phiên bản |
|---|---|---|
| **Language** | Java | JDK 17 |
| **Server** | Apache Tomcat | 10.1.53 |
| **Servlet API** | Jakarta Servlet | 6.0 |
| **JSP API** | Jakarta JSP | 3.1 |
| **JSTL** | Jakarta JSTL | 3.0.1 |
| **Database** | SQL Server | 2022 |
| **JDBC Driver** | MSSQL JDBC | 12.8.1 |
| **Frontend** | Bootstrap | 5.3.3 |
| **Icons** | Bootstrap Icons | 1.11.3 |
| **Font** | Inter (Google Fonts) | - |
| **IDE** | NetBeans | 17 |
| **Build Tool** | Maven | 3.x |

## 🚀 Hướng dẫn cài đặt

### Yêu cầu hệ thống
- JDK 17+
- Apache Tomcat 10.1.x
- SQL Server 2019+ (với TCP/IP enabled, port 1433)
- NetBeans 17 (hoặc IDE hỗ trợ Maven)

### Bước 1: Cấu hình SQL Server

1. Mở **SQL Server Configuration Manager**
2. Vào **SQL Server Network Configuration** → **Protocols for MSSQLSERVER**
3. **Enable TCP/IP** → Double-click → IP Addresses → IPAll → TCP Port = `1433`
4. Vào **SQL Server Services** → Restart **SQL Server (MSSQLSERVER)**
5. Đảm bảo SQL Server ở chế độ **Mixed Mode Authentication**

### Bước 2: Tạo Database

Mở **SQL Server Management Studio (SSMS)**, chạy lần lượt:

```sql
-- 1. Tạo schema
-- Mở file: sql/database-schema.sql → Execute (F5)

-- 2. Thêm dữ liệu mẫu
-- Mở file: sql/test-data.sql → Execute (F5)
```

### Bước 3: Cấu hình kết nối

Mở file `src/main/java/com/carrental/config/DBContext.java`, sửa thông tin:

```java
private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=CarRentalDB;...";
private static final String USERNAME = "sa";          // Tài khoản SQL Server
private static final String PASSWORD = "your_password"; // Mật khẩu của bạn
```

### Bước 4: Chạy project

1. Mở **NetBeans 17** → **Open Project** → chọn thư mục `CarRentalSystem`
2. Cấu hình server: **Properties** → **Run** → Server = **Apache Tomcat 10.1.x**
3. **Clean and Build** (Shift+F11)
4. **Run** (F6)
5. Truy cập: `http://localhost:8080/CarRentalSystem/`

## 📋 Tài khoản test

| Vai trò | Username | Password | Chức năng |
|---|---|---|---|
| 🛡️ Admin | `admin` | `admin123` | Quản lý users, phân quyền |
| 👨‍💼 Staff | `staff01` | `staff123` | Duyệt/xử lý hợp đồng |
| 🔧 Manager | `manager01` | `manager123` | Quản lý xe, phân công tài xế |
| 🚘 Driver 1 | `driver01` | `driver123` | Xem lịch lái xe |
| 🚘 Driver 2 | `driver02` | `driver123` | Xem lịch lái xe |
| 👤 Customer 1 | `customer01` | `cust123` | Tìm & đặt xe, xem hợp đồng |
| 👤 Customer 2 | `customer02` | `cust123` | Tìm & đặt xe, xem hợp đồng |

## 📊 Quy trình nghiệp vụ

```
                                    ┌──────────────┐
                                    │   CANCELLED   │
                                    └──────┬───────┘
                                           │ (có thể hủy bất cứ lúc nào)
                                           │
┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   PENDING    │───▶│   ACCEPTED   │───▶│ DEPOSIT_PAID │───▶│CAR_PICKED_UP │
│   REVIEW     │    │              │    │              │    │              │
└──────┬───────┘    └──────────────┘    └──────────────┘    └──────┬───────┘
       │                                                          │
       ▼                                                          ▼
┌──────────────┐                                           ┌──────────────┐
│   REJECTED   │                                           │ CAR_RETURNED │
└──────────────┘                                           └──────┬───────┘
                                                                  │
                                                                  ▼
                                                           ┌──────────────┐
                                                           │FINAL_PAYMENT │
                                                           │  COMPLETED   │
                                                           └──────────────┘
```

## 🔒 Phân quyền truy cập

| URL | Vai trò được phép |
|---|---|
| `/search` | Tất cả (không cần đăng nhập) |
| `/login`, `/register` | Tất cả |
| `/book`, `/my-contracts` | CUSTOMER |
| `/staff/*` | STAFF, MANAGER, ADMIN |
| `/manager/*` | MANAGER, ADMIN |
| `/driver/*` | DRIVER |
| `/admin/*` | ADMIN |

## 🗄️ Database Schema

Hệ thống gồm **10 bảng** chính:

- **Users** - Tài khoản người dùng
- **Roles** - Vai trò (ADMIN, MANAGER, STAFF, DRIVER, CUSTOMER)
- **User_Roles** - Phân quyền (N-N)
- **Car_Types** - Loại xe (4/5/7 chỗ)
- **Cars** - Thông tin xe
- **Customers** - Hồ sơ khách hàng
- **Drivers** - Hồ sơ tài xế
- **Employees** - Hồ sơ nhân viên
- **Contracts** - Hợp đồng thuê xe
- **Contract_Details** - Chi tiết xe trong hợp đồng
- **Driver_Assignments** - Phân công tài xế

**3 Triggers** bảo vệ tính toàn vẹn dữ liệu:
- Chống trùng lịch xe (car overlap)
- Chống xung đột lịch tài xế (driver schedule conflict)
- Validate phân công tài xế

## 📝 License

Dự án này được phát triển cho môn **PRJ301** - FPT University.