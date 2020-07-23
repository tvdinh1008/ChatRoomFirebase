# ChatRoomFirebase
Sử dụng livedata + MVVM
Cơ sở dữ liệu trên firebase realtime database (dữ liệu lưu kiểu json)
sử dụng storage trên firabase để lưu ảnh đại diện cho user


Các chức năng:
+ tạo tài khoản bằng email và xác thực qua email, password
    auth.createUserWithEmailAndPassword(email,password)
    reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

+ user sẽ autologin(đã đăng nhập) và logout
    auth.signInWithEmailAndPassword(txt_email,txt_password)//đăng nhập lần đầu
    firebaseUser= FirebaseAuth.getInstance().getCurrentUser();//lần sau chỉ cần kiểm tra xem đã đăng nhập chưa??
  
+ Hiển thị danh sách user sử dụng app
  DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
  sau đó lấy danh sách user đẩy vào recyclerview
  
+ Hiển thị danh sách mà user đã chat
   reference= FirebaseDatabase.getInstance().getReference("Chats");
   sau đó kiểm tra id người gửi, id người nhận
+ Hiển thị trạng thái online/offline
  Mỗi user sẽ có 1 trường là status
  //update status
  reference=FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
  HashMap<String,Object> hashMap=new HashMap<>();
  hashMap.put("status",status);
   reference.updateChildren(hashMap);
  //Khi vào giao diện đăng nhập xong thì khi ở onResume() thì status="online" còn ở onPause() thì status="offline"
   Do reference.addValueEventListener() sẽ bắt tất cả sự kiện thay đổi dữ liệu, hay để lấy tất cả dữ liệu => Khi thay đổi status thì nó cũng update theo ở giao diện
 

+ Hiển thị trạng thái seen của chat
  Khi A gửi tin cho B: trạng thái tin là chưa xem
  firebaseUser= FirebaseAuth.getInstance().getCurrentUser();//id A
  reference= FirebaseDatabase.getInstance().getReference("Users").child(userid); //id B
  reference.addValueEventListener(new ValueEventListener() {
    //tại đây ta sẽ cập nhật lại dữ liệu là đã xem
    //firebaseUser.getUid() là A,  userid là B
    if(chat.getReceiver().equals(firebaseUser.getUid())&& chat.getSender().equals(userid))
    {
      hashMap.put("isseen",true);//đây là TH B(getSender) gửi cho A(getReceiver)
    }
  }
  khi B yêu cầu hay click vào nhắn tin cho A ->ta sẽ lấy đc qua hàm 
  =>Khi B đăng nhập thì firebaseUser.getUid() là B(tức B là người nhận, userid là người gửi =>Trạng thái cập nhật

+ Sửa đổi ảnh đại diện(cơ sở dữ liệu sẽ lưu lên storage của firebase)
    storageReference= FirebaseStorage.getInstance().getReference("uploads"); //upload ảnh lên storage vào đường dẫn uploads(tự tạo)
    firebaseUser= FirebaseAuth.getInstance().getCurrentUser();//lấy id người đang dùng
    reference=FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());//truy cập đến dữ liệu của người này
   final StorageReference fileReference=storageReference.child(System.currentTimeMillis()
            +"."+getFileExtension(imageUri));
    uploadTask=fileReference.putFile(imageUri);
  
