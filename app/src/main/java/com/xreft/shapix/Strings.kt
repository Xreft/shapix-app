package com.xreft.shapix

enum class Lang(val code: String, val label: String, val rtl: Boolean) {
    Ar("ar", "العربية", true),
    Fa("fa", "فارسی", true),
    En("en", "English", false)
}

fun tr(lang: Lang, key: String): String =
    STRINGS[lang]?.get(key) ?: STRINGS[Lang.En]!![key] ?: key

val STRINGS: Map<Lang, Map<String, String>> = mapOf(
    Lang.Ar to mapOf(
        "appName" to "قصّاص", "tag" to "نسخة 2.0 • يعمل على جهازك", "titleSub" to "محرر الصور",
        "subtitle" to "اختر صورتك، حدد الشكل، ارسم وأضف فلاتر، ثم احفظها بخلفية شفافة.",
        "toolsHeading" to "أدوات التحرير", "dropTitle" to "اضغط لاختيار صورة",
        "dropHint" to "PNG وJPEG وWebP وGIF ثابت • حد 20 ميغابايت",
        "shapeHeading" to "شكل القص", "shapeCircle" to "دائرة", "shapeTriangle" to "مثلث",
        "shapeSquare" to "مربع", "shapeRounded" to "مربع منحني", "shapeStar" to "نجمة", "shapeHexagon" to "سداسي",
        "zoomLabel" to "التكبير", "rotationLabel" to "التدوير", "sizeLabel" to "حجم الشكل",
        "flipHeading" to "القلب", "flipH" to "أفقي", "flipV" to "رأسي",
        "filtersHeading" to "الفلاتر", "filterNone" to "بدون", "filterBw" to "أبيض وأسود",
        "filterSepia" to "سيبيا", "filterInvert" to "معكوس", "filterVintage" to "قديم",
        "filterCool" to "بارد", "filterWarm" to "دافئ",
        "brightnessLabel" to "السطوع", "contrastLabel" to "التباين", "saturateLabel" to "التشبع", "blurLabel" to "الضبابية",
        "modeHeading" to "الوضع", "modeMove" to "تحريك", "modeDraw" to "رسم",
        "colorLabel" to "اللون", "brushLabel" to "حجم الفرشاة", "undoBtn" to "↩ تراجع", "clearBtn" to "✕ مسح الرسم",
        "transparentLabel" to "خلفية شفافة (PNG)", "bgColorLabel" to "لون الخلفية",
        "resetBtn" to "↺ إعادة ضبط", "downloadBtn" to "↓ حفظ PNG",
        "previewHeading" to "المعاينة المباشرة", "previewHint" to "اسحب الصورة لتحريكها",
        "emptyText" to "اختر صورة لتبدأ التعديل",
        "bottomText" to "اسحب الصورة بإصبعك لتحديد الجزء الظاهر داخل الشكل، أو بدّل لوضع الرسم لإضافة خطوط ملونة. يتم التعديل محليًا على جهازك ولا تُرسل صورك إلى أي خادم. الصورة الناتجة 800 × 800 بكسل.",
        "footerText" to "© 2026 Xreft | قصّاص — محرر صور مجاني. جميع الحقوق محفوظة.",
        "msgInvalidFile" to "اختر ملف صورة صالحًا.", "msgTooBig" to "حجم الصورة أكبر من 20 ميغابايت.",
        "msgLoadFail" to "تعذّر قراءة الصورة. جرّب PNG أو JPEG.",
        "msgUploaded" to "تم تحميل الصورة. اسحبها لتحديد موضع القص.", "msgReset" to "أُعيد ضبط الصورة.",
        "msgExportFail" to "تعذّر حفظ الصورة.", "msgDownloaded" to "تم حفظ الصورة في المعرض.",
        "msgDrawCleared" to "تم مسح الرسم."
    ),
    Lang.Fa to mapOf(
        "appName" to "شپیکس", "tag" to "نسخه ۲٫۰ • اجرا روی دستگاه شما", "titleSub" to "ویرایشگر عکس",
        "subtitle" to "عکس را انتخاب کنید، شکل برش را برگزینید، نقاشی و فیلتر اضافه کنید و با پس‌زمینه شفاف ذخیره کنید.",
        "toolsHeading" to "ابزارهای ویرایش", "dropTitle" to "برای انتخاب عکس ضربه بزنید",
        "dropHint" to "PNG، JPEG، WebP، GIF ثابت • حداکثر ۲۰ مگابایت",
        "shapeHeading" to "شکل برش", "shapeCircle" to "دایره", "shapeTriangle" to "مثلث",
        "shapeSquare" to "مربع", "shapeRounded" to "مربع گرد", "shapeStar" to "ستاره", "shapeHexagon" to "شش‌ضلعی",
        "zoomLabel" to "بزرگ‌نمایی", "rotationLabel" to "چرخش", "sizeLabel" to "اندازه شکل",
        "flipHeading" to "قرینه کردن", "flipH" to "افقی", "flipV" to "عمودی",
        "filtersHeading" to "فیلترها", "filterNone" to "بدون فیلتر", "filterBw" to "سیاه‌وسفید",
        "filterSepia" to "سپیا", "filterInvert" to "معکوس", "filterVintage" to "قدیمی",
        "filterCool" to "سرد", "filterWarm" to "گرم",
        "brightnessLabel" to "روشنایی", "contrastLabel" to "کنتراست", "saturateLabel" to "اشباع رنگ", "blurLabel" to "محو کردن",
        "modeHeading" to "حالت", "modeMove" to "جابه‌جایی", "modeDraw" to "نقاشی",
        "colorLabel" to "رنگ", "brushLabel" to "اندازه قلم", "undoBtn" to "↩ بازگرداندن", "clearBtn" to "✕ پاک کردن نقاشی",
        "transparentLabel" to "پس‌زمینه شفاف (PNG)", "bgColorLabel" to "رنگ پس‌زمینه",
        "resetBtn" to "↺ بازنشانی", "downloadBtn" to "↓ ذخیره PNG",
        "previewHeading" to "پیش‌نمایش زنده", "previewHint" to "برای جابه‌جایی، تصویر را بکشید",
        "emptyText" to "برای شروع ویرایش، یک عکس انتخاب کنید",
        "bottomText" to "برای تعیین بخش قابل‌مشاهده داخل شکل، تصویر را با انگشت بکشید، یا به حالت نقاشی بروید و خطوط رنگی اضافه کنید. ویرایش روی دستگاه شما انجام می‌شود و عکس‌ها به هیچ سروری ارسال نمی‌شوند. خروجی ۸۰۰×۸۰۰ پیکسل است.",
        "footerText" to "© 2026 Xreft | شپیکس — ویرایشگر رایگان عکس. همهٔ حقوق محفوظ است.",
        "msgInvalidFile" to "لطفاً یک فایل تصویری معتبر انتخاب کنید.", "msgTooBig" to "حجم تصویر بیشتر از ۲۰ مگابایت است.",
        "msgLoadFail" to "خواندن تصویر ممکن نشد. PNG یا JPEG را امتحان کنید.",
        "msgUploaded" to "تصویر بارگذاری شد. برای تعیین محل برش، آن را بکشید.", "msgReset" to "تصویر بازنشانی شد.",
        "msgExportFail" to "ذخیره تصویر ممکن نشد.", "msgDownloaded" to "تصویر در گالری ذخیره شد.",
        "msgDrawCleared" to "نقاشی پاک شد."
    ),
    Lang.En to mapOf(
        "appName" to "Shapix", "tag" to "v2.0 • runs on your device", "titleSub" to "Image Editor",
        "subtitle" to "Pick a photo, choose a shape, draw and add filters, then save it with a transparent background.",
        "toolsHeading" to "Editing Tools", "dropTitle" to "Tap to choose an image",
        "dropHint" to "PNG, JPEG, WebP, static GIF • 20MB max",
        "shapeHeading" to "Crop Shape", "shapeCircle" to "Circle", "shapeTriangle" to "Triangle",
        "shapeSquare" to "Square", "shapeRounded" to "Rounded", "shapeStar" to "Star", "shapeHexagon" to "Hexagon",
        "zoomLabel" to "Zoom", "rotationLabel" to "Rotation", "sizeLabel" to "Shape size",
        "flipHeading" to "Flip", "flipH" to "Horizontal", "flipV" to "Vertical",
        "filtersHeading" to "Filters", "filterNone" to "None", "filterBw" to "B&W",
        "filterSepia" to "Sepia", "filterInvert" to "Invert", "filterVintage" to "Vintage",
        "filterCool" to "Cool", "filterWarm" to "Warm",
        "brightnessLabel" to "Brightness", "contrastLabel" to "Contrast", "saturateLabel" to "Saturation", "blurLabel" to "Blur",
        "modeHeading" to "Mode", "modeMove" to "Move", "modeDraw" to "Draw",
        "colorLabel" to "Color", "brushLabel" to "Brush size", "undoBtn" to "↩ Undo", "clearBtn" to "✕ Clear drawing",
        "transparentLabel" to "Transparent background (PNG)", "bgColorLabel" to "Background color",
        "resetBtn" to "↺ Reset", "downloadBtn" to "↓ Save PNG",
        "previewHeading" to "Live Preview", "previewHint" to "Drag the image to move it",
        "emptyText" to "Choose an image to start editing",
        "bottomText" to "Drag the image with your finger to position it inside the shape, or switch to draw mode to add colored strokes. Editing happens locally on your device; your photos are never uploaded. Output is 800 × 800px.",
        "footerText" to "© 2026 Xreft | Shapix — Free Image Editor. All rights reserved.",
        "msgInvalidFile" to "Please choose a valid image file.", "msgTooBig" to "Image is larger than 20MB.",
        "msgLoadFail" to "Could not read the image. Try PNG or JPEG.",
        "msgUploaded" to "Image loaded. Drag it to position the crop.", "msgReset" to "Image reset.",
        "msgExportFail" to "Could not save the image.", "msgDownloaded" to "Image saved to your gallery.",
        "msgDrawCleared" to "Drawing cleared."
    )
)
