
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/keyboard_root"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:background="#1F1F1F"
    android:padding="4dp">

    <!-- 🔐 රහස් වැඩකටයුතු බටන්ස් දෙක (Encode / Decode) -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="45dp"
        android:orientation="horizontal"
        android:layout_marginBottom="5dp">
        
        <Button
            android:id="@+id/btn_encode"
            android:layout_width="0dp"
            android:layout_height="match_parent"
            android:layout_weight="1"
            android:layout_margin="2dp"
            android:text="🔐 ENCODE"
            android:textColor="#FFFFFF"
            android:backgroundTint="#2D2D2D"
            android:textSize="12sp" />
            
        <Button
            android:id="@+id/btn_decode"
            android:layout_width="0dp"
            android:layout_height="match_parent"
            android:layout_weight="1"
            android:layout_margin="2dp"
            android:text="🔓 DECODE"
            android:textColor="#FFFFFF"
            android:backgroundTint="#2D2D2D"
            android:textSize="12sp" />
    </LinearLayout>

    <!-- අපි පරණ සිස්ටම් එක වෙනුවට අලුත් XML එක කෙළින්ම Android KeyboardView එකෙන්ම ලෝඩ් කරනවා -->
    <android.inputmethodservice.KeyboardView
        android:id="@+id/keyboard_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="#1F1F1F"
        android:keyTextColor="#FFFFFF"
        android:keyBackground="@android:drawable/btn_default"
        android:shadowColor="#000000"
        android:shadowRadius="0.0" />

</LinearLayout>
