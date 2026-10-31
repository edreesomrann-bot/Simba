package com.example.util

enum class AppLanguage {
    ARABIC,
    ENGLISH
}

object Localization {

    fun get(key: String, language: AppLanguage): String {
        val entry = strings[key] ?: return key
        return when (language) {
            AppLanguage.ARABIC -> entry.first
            AppLanguage.ENGLISH -> entry.second
        }
    }

    private val strings = mapOf(
        "app_name" to Pair("دوّرني", "Dawarni"),
        "app_subtitle" to Pair("دليل الحرفيين والعملاء المباشر • ليبيا 🇱🇾", "Craftsmen & Clients Direct Platform • Libya 🇱🇾"),
        "ad_free_badge" to Pair("خالٍ تماماً من الإعلانات 100% • خدمة سريعة ومجانية", "100% Ad-Free • Fast & Free Direct Service"),
        "libya_only_note" to Pair("مخصص حالياً للمدن الليبية: طرابلس، بنغازي، مصراتة، والزاوية", "Currently dedicated to Libyan cities: Tripoli, Benghazi, Misrata, and Zawiya"),
        "vip_badge" to Pair("حرفي مميز 👑", "VIP Pro 👑"),
        "vip_title" to Pair("باقة التميز وأولوية الظهور", "Priority Placement VIP"),
        "vip_price" to Pair("100 دينار ليبي شهرياً", "100 LYD / month"),
        "vip_desc" to Pair("احصل على أولوية الظهور الدائم في أعلى نتائج البحث والتصفح وشارة التميز الذهبية لجذب أكبر عدد من العملاء القريبين.", "Get permanent top placement in search and browsing with the golden VIP badge to attract the highest number of nearby clients."),
        "vip_active" to Pair("الاشتراك نشط 👑 (أولوية قصوى)", "Subscription Active 👑 (Top Priority)"),
        "vip_upgrade" to Pair("الاشتراك في باقة التميز (100 د.ل)", "Subscribe to VIP (100 LYD)"),
        "client_section" to Pair("قسم العميل", "Client View"),
        "craftsman_section" to Pair("قسم الحرفي", "Craftsman View"),
        "client_subtitle" to Pair("وصول سريع ومباشر للحرفيين القريبين منك بدون تسجيل", "Instant direct access to nearby craftsmen without sign-in"),
        "search_hint" to Pair("ابحث باسم الحرفي، الحرفة أو الحي...", "Search by craftsman, craft, or district..."),
        "all" to Pair("الكل", "All"),
        "plumbing" to Pair("سباكة", "Plumbing"),
        "electrical" to Pair("كهرباء", "Electrical"),
        "ac_cooling" to Pair("تكييف وتبريد", "AC & Cooling"),
        "carpentry" to Pair("نجارة", "Carpentry"),
        "painting" to Pair("دهان وديكور", "Painting & Decor"),
        "appliances" to Pair("صيانة أجهزة", "Appliance Repair"),
        "blacksmith" to Pair("حدادة وألمنيوم", "Blacksmith & Alum"),
        "tiles" to Pair("سيراميك وبلاط", "Tiles & Ceramic"),

        // Filter & Sort
        "radius" to Pair("نطاق البحث", "Search Radius"),
        "all_distances" to Pair("كل المسافات", "All Distances"),
        "sort_nearest" to Pair("الأقرب مسافة", "Nearest"),
        "sort_rating" to Pair("الأعلى تقييماً", "Highest Rated"),
        "sort_experience" to Pair("الأكثر خبرة", "Most Experienced"),
        "filter_available" to Pair("متاح الآن فقط", "Available Now Only"),
        "filter_emergency" to Pair("طوارئ 24/7", "Emergency 24/7"),
        "filter_rating_4" to Pair("تقييم 4.5+", "Rating 4.5+"),

        // Status
        "status_available" to Pair("متاح الآن للعمل", "Available Now"),
        "status_busy" to Pair("مشغول حالياً", "Currently Busy"),
        "emergency_badge" to Pair("طوارئ 24/7", "24/7 Emergency"),
        "km_away" to Pair("كم منك", "km away"),
        "exp_years" to Pair("سنوات خبرة", "years exp"),

        // Actions
        "call_direct" to Pair("اتصال مباشر", "Call Now"),
        "whatsapp_chat" to Pair("محادثة واتساب", "WhatsApp"),
        "request_service" to Pair("إرسال طلب عمل", "Send Request"),
        "view_profile" to Pair("عرض الملف والتقييمات", "View Profile & Reviews"),
        "favorite" to Pair("المفضلة", "Favorite"),
        "unfavorite" to Pair("إزالة من المفضلة", "Unfavorite"),

        // Reviews & Rating
        "ratings_reviews" to Pair("التقييمات والمراجعات", "Ratings & Reviews"),
        "quality_rating" to Pair("جودة العمل وإتقانه", "Quality of Work"),
        "punctuality_rating" to Pair("الالتزام بالمواعيد", "Punctuality"),
        "communication_rating" to Pair("سهولة ولطافة التواصل", "Communication"),
        "add_review" to Pair("إضافة تقييم للخدمة", "Add Service Review"),
        "average_rating" to Pair("متوسط التقييمات", "Average Rating"),
        "review_count" to Pair("تقييم", "reviews"),
        "no_reviews_yet" to Pair("لا توجد مراجعات حتى الآن. كن أول من يقيّم!", "No reviews yet. Be the first to review!"),
        "submit_review" to Pair("إرسال التقييم", "Submit Review"),
        "your_name" to Pair("اسمك الكريم", "Your Name"),
        "review_comment_hint" to Pair("اكتب تجربتك مع الحرفي لمساعدة الآخرين...", "Write your experience with the craftsman..."),
        "rate_craftsman_title" to Pair("تقييم الحرفي", "Rate Craftsman"),

        // Service Request
        "send_request_title" to Pair("طلب خدمة جديدة", "Request New Service"),
        "problem_description" to Pair("وصف العطل أو العمل المطلوب", "Problem or Work Description"),
        "problem_desc_hint" to Pair("مثال: تسريب مياه تحت المغسلة، يحتاج فحص سريع...", "e.g., Water leak under sink, urgent check needed..."),
        "client_phone" to Pair("رقم هاتفك للتواصل", "Your Phone Number"),
        "client_address" to Pair("الحي أو العنوان التقريبي", "Your District or Address"),
        "is_urgent_emergency" to Pair("طلب عاجل / طوارئ", "Urgent / Emergency Request"),
        "send_now" to Pair("إرسال الطلب الآن", "Send Request Now"),
        "cancel" to Pair("إلغاء", "Cancel"),

        // Craftsman Section
        "my_craftsman_profile" to Pair("ملف الحرفي المهني", "Craftsman Profile"),
        "incoming_requests" to Pair("الطلبات الواردة", "Incoming Requests"),
        "my_reviews_title" to Pair("تقييماتي والمراجعات", "My Reviews & Ratings"),
        "edit_profile" to Pair("تعديل بياناتي", "Edit Profile"),
        "save_profile" to Pair("حفظ التغييرات", "Save Changes"),
        "craftsman_name" to Pair("الاسم الكامل", "Full Name"),
        "craftsman_profession" to Pair("المهنة / الحرفة", "Craft / Profession"),
        "craftsman_phone" to Pair("رقم الاتصال", "Phone Number"),
        "craftsman_whatsapp" to Pair("رقم الواتساب", "WhatsApp Number"),
        "craftsman_city" to Pair("المدينة", "City"),
        "craftsman_district" to Pair("الحي أو منطقة التواجد", "District / Location"),
        "craftsman_bio" to Pair("نبذة عن خبراتك وخدماتك", "About Experience & Services"),
        "craftsman_pricing" to Pair("تسعيرة تقريبية أو كشفية", "Estimated Price / Inspection Fee"),
        "toggle_availability" to Pair("حالة استقبال الطلبات", "Accepting Requests Status"),
        "toggle_emergency" to Pair("تقديم خدمة طوارئ 24 ساعة", "Offer 24/7 Emergency Service"),
        "no_incoming_requests" to Pair("لا توجد طلبات واردة حالياً. ستظهر الطلبات الجديدة هنا فور إرسالها من العملاء القريبين.", "No incoming requests yet. New requests from nearby clients will appear here."),

        // Request Statuses
        "status_pending" to Pair("طلب جديد بانتظار الرد", "Pending Response"),
        "status_accepted" to Pair("تم قبول الطلب", "Accepted"),
        "status_on_the_way" to Pair("في الطريق إلى العميل", "On the Way"),
        "status_in_progress" to Pair("جاري العمل على الطلب", "In Progress"),
        "status_completed" to Pair("تم إنجاز العمل بنجاح", "Completed"),
        "status_declined" to Pair("اعتذر عن الطلب", "Declined"),

        // Craftsman Action Buttons on Requests
        "accept_request" to Pair("قبول الطلب", "Accept"),
        "on_way_btn" to Pair("في الطريق للعميل", "On the Way"),
        "start_work_btn" to Pair("بدء العمل", "Start Work"),
        "complete_work_btn" to Pair("إنهاء العمل", "Complete"),
        "decline_btn" to Pair("اعتذار", "Decline"),

        // Notifications
        "notifications_title" to Pair("الإشعارات الفورية", "Live Notifications"),
        "mark_all_read" to Pair("تعيين الكل كمقروء", "Mark all read"),
        "no_notifications" to Pair("لا توجد إشعارات حتى الآن", "No notifications yet"),

        // Radar & Map
        "radar_title" to Pair("رادار الحرفيين القريبين", "Nearby Craftsmen Radar"),
        "radar_scan" to Pair("تحديد موقعي وفحص المحيط", "Detect Location & Scan"),
        "change_location" to Pair("تغيير الحي التجريبي", "Change Test District"),
        "craftsmen_found" to Pair("حرفي متاح ضمن النطاق", "craftsmen within radius"),

        // Tabs
        "tab_explore" to Pair("استكشاف", "Explore"),
        "tab_radar" to Pair("الخريطة والرادار", "Radar & Map"),
        "tab_my_requests" to Pair("طلباتي", "My Requests"),
        "tab_favorites" to Pair("المفضلة", "Favorites")
    )
}
