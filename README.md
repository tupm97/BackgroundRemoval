# BackgroundRemoval
BackgroundRemoval là ứng dụng cho phép chụp ảnh sau đó giữ lấy những đối tượng là người và xóa phông nền
Ứng dụng sử Machine Learning model để dự đoán và xóa phông nền

## Library

Trong ứng dụng có package là library
Có 4 component chính:</br>
-VisionPredictor: dùng để quản lý và thực thi model</br>
-VisionImage: xử lý ảnh chụp được để chuẩn bị cho việc dự đoán của model </br>
-OnDeviceModel: chứa thông tin và load model file cho việc dự đoán </br>
-VisionResult: Truy cập vào kết quả của việc dự đoán</br>

Class VisionPredictor xử lý tất cả các xử lý phức tạp đi kèm với việc chạy model trên thiết bị, trong đó có việc tiền xử lý (pre-processing - đây là các thao tác cần thiết để chuẩn bị ảnh để định dạng input cho model) và hậu xử lý (post-processing - xử lý đầu ra từ model để lấy kết quả mong muốn)</br>
Với mỗi predictor chứa phương thức predict lấy VisionImage làm đối số và trả về là VisionResult

## Cài đặt
Clone project về máy và chạy ứng dụng</br>
Khi mở ứng dụng lần đầu ứng dụng sẽ tải model về máy và lưu nó tại bộ nhớ internal của máy với tên viettelbackgroundremoval.tflite</br>
Sau đó ứng dụng sẽ chuyển sang màn hình camera để chụp ảnh</br>
Người dùng có thể chuyển đổi giữa camera trước và sau để chụp ảnh bằng cách nhấn nút chuyển đổi ở góc phải dưới màn hình</br>
Tại màn hình camera sẽ có đường kẻ nét đứt, đó là vị trí đỉnh đầu một người nào đó</br>
Sau khi chụp hình, ứng dụng sẽ hiện lên ảnh kèm theo nút Xóa phông</br>
Khi ấn vào nút Xóa phông thì phông nền được xóa và chuyển phông nền thành màu trắng xám.
