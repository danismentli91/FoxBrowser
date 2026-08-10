# FoxBrowser Android

Gerçek Android kaynak projesidir. GeckoView tabanlıdır ve rootsuz çalışacak şekilde tasarlanmıştır.

## Klavye

- Tarayıcının üstünde her zaman görünen `Esc`, `Ctrl`, `Alt`, `Tab`, `F1–F12` şeridi vardır.
- FoxKeyboard ayrıca Android giriş yöntemi (IME) olarak tanımlıdır.
- Telefonda **Ayarlar → Sistem → Diller ve giriş → Ekran klavyesi → Klavyeleri yönet** bölümünden FoxKeyboard etkinleştirilmeden Android varsayılan klavyeyi değiştirmez. Bu Android güvenlik kuralıdır.

## Motor ve içerik

- GeckoView ile HTML5, modern JavaScript, WebAssembly, ses ve video desteği.
- Android medya çözücüleri üzerinden desteklenen MP4/WebM/MP3/Opus türleri.
- Office, PDF, e-kitap ve arşiv dosyaları için dosya yönlendirme katmanı planlanmıştır; RAR için Junrar bağımlılığı eklenmiştir.
- Java Applet desteklenmez. Modern Android tarayıcı motorları NPAPI/Java Applet çalıştırmaz. `.java`, `.class` ve `.jar` dosyaları görüntülenebilir; Java programı çalıştırmak için Termux + OpenJDK entegrasyonu kullanılmalıdır.

## Derleme

Android Studio Ladybug veya daha yeni sürümde projeyi açın, Gradle senkronizasyonunu tamamlayın ve **Build → Build APK(s)** seçin. Komut satırında Android SDK ve Gradle kuruluysa `gradle assembleDebug` kullanılabilir.

Bu ilk mühendislik iskeleti; VPN, seri port, dosya görüntüleyici, indirme yöneticisi ve parola kasasının üretim kodları sonraki modüller olarak tamamlanmalıdır.
