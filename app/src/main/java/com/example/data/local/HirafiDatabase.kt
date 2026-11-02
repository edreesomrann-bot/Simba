package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AppNotificationEntity
import com.example.data.model.CraftsmanEntity
import com.example.data.model.PortfolioItemEntity
import com.example.data.model.ReviewEntity
import com.example.data.model.ServiceRequestEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CraftsmanEntity::class,
        ReviewEntity::class,
        ServiceRequestEntity::class,
        AppNotificationEntity::class,
        PortfolioItemEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class HirafiDatabase : RoomDatabase() {

    abstract fun hirafiDao(): HirafiDao

    companion object {
        @Volatile
        private var INSTANCE: HirafiDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): HirafiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HirafiDatabase::class.java,
                    "hirafi_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.hirafiDao())
                    }
                }
            }

            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.hirafiDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: HirafiDao) {
            val sampleCraftsmen = listOf(
                CraftsmanEntity(
                    id = 1,
                    name = "عبد السلام الورفلي",
                    craft = "سباكة",
                    experienceYears = 9,
                    phone = "+218912345678",
                    whatsapp = "+218912345678",
                    city = "طرابلس",
                    district = "حي الأندلس",
                    latitude = 32.8910,
                    longitude = 13.1520,
                    bio = "أسطى سباكة متخصص في صيانة وتأسيس شبكات المياه والصرف الصحي، كشف التسريبات وتركيب المضخات والخلاطات بسرعة واحترافية.",
                    priceEstimate = "كشف يبدأ من 35 د.ل",
                    isAvailable = true,
                    hasEmergencyService = true,
                    isVipFeatured = true,
                    specializations = "كشف تسريبات,تركيب مضخات,شبكات PEX,صيانة سخانات",
                    avatarSeed = "abdulsalam"
                ),
                CraftsmanEntity(
                    id = 2,
                    name = "محمود الفيتوري",
                    craft = "كهرباء",
                    experienceYears = 12,
                    phone = "+218923456789",
                    whatsapp = "+218923456789",
                    city = "طرابلس",
                    district = "بن عاشور",
                    latitude = 32.8760,
                    longitude = 13.1860,
                    bio = "فني كهرباء منازل ومحلات، صيانة لوحات التوزيع والقواطع، تركيب إنارة مخفية ولمبات ليد وحل مشاكل الشورت الكهربائي.",
                    priceEstimate = "كشف وتصليح يبدأ من 40 د.ل",
                    isAvailable = true,
                    hasEmergencyService = true,
                    isVipFeatured = true,
                    specializations = "كشف شورت,لوحات قواطع,إنارة مخفية,تأسيس 3 فاز",
                    avatarSeed = "mahmoud"
                ),
                CraftsmanEntity(
                    id = 3,
                    name = "فرج العريبي",
                    craft = "تكييف وتبريد",
                    experienceYears = 8,
                    phone = "+218914567890",
                    whatsapp = "+218914567890",
                    city = "بنغازي",
                    district = "الكيش",
                    latitude = 32.1120,
                    longitude = 20.0710,
                    bio = "صيانة وتنظيف مكيفات سبليت ودولابي، تعبئة غاز التبريد الأصلي، كشف تسريب الغاز وفحص الكمبروسر.",
                    priceEstimate = "غسيل وصيانة 50 د.ل",
                    isAvailable = true,
                    hasEmergencyService = true,
                    specializations = "غسيل مكيفات,شحن فريون,إصلاح كمبروسر,صيانة انفيرتر",
                    avatarSeed = "faraj"
                ),
                CraftsmanEntity(
                    id = 4,
                    name = "أصيل التاجوري",
                    craft = "نجارة",
                    experienceYears = 14,
                    phone = "+218945678901",
                    whatsapp = "+218945678901",
                    city = "طرابلس",
                    district = "سوق الجمعة",
                    latitude = 32.8830,
                    longitude = 13.2320,
                    bio = "نجار محترف لفك وتركيب وصيانة غرف النوم والمطابخ، صيانة الأبواب والأقفال والكوالين وتفصيل الدواليب الخشبية.",
                    priceEstimate = "معاينة وصيانة حسب العمل",
                    isAvailable = true,
                    hasEmergencyService = false,
                    avatarSeed = "aseel"
                ),
                CraftsmanEntity(
                    id = 5,
                    name = "علي الشريف",
                    craft = "دهان وديكور",
                    experienceYears = 10,
                    phone = "+218926789012",
                    whatsapp = "+218926789012",
                    city = "مصراتة",
                    district = "الرويسات",
                    latitude = 32.3810,
                    longitude = 15.0920,
                    bio = "تنفيذ جميع أنواع الدهانات الحديثة والديكورية، ورق حائط، بديل الخشب وبديل الرخام وتشطيب المنازل بجودة عالية.",
                    priceEstimate = "معاينة مجانية وعرض سعر",
                    isAvailable = true,
                    hasEmergencyService = false,
                    avatarSeed = "ali"
                ),
                CraftsmanEntity(
                    id = 6,
                    name = "طارق المقريف",
                    craft = "صيانة أجهزة منزلية",
                    experienceYears = 7,
                    phone = "+218917890123",
                    whatsapp = "+218917890123",
                    city = "بنغازي",
                    district = "الهواري",
                    latitude = 32.0820,
                    longitude = 20.1020,
                    bio = "صيانة الغسالات الأوتوماتيكية، الثلاجات والمجمدات والأفران المنزلية، توفير قطع الغيار وضمان الصيانة.",
                    priceEstimate = "كشف منزلي 30 د.ل",
                    isAvailable = true,
                    hasEmergencyService = true,
                    avatarSeed = "tarek"
                ),
                CraftsmanEntity(
                    id = 7,
                    name = "إبراهيم الزنتاني",
                    craft = "حدادة وألمنيوم",
                    experienceYears = 11,
                    phone = "+218928901234",
                    whatsapp = "+218928901234",
                    city = "طرابلس",
                    district = "جنزور",
                    latitude = 32.8270,
                    longitude = 13.0180,
                    bio = "تفصيل وتركيب شبابيك وأبواب ألمنيوم ومطابخ، درابزين حديد، مظلات سيارات ولحام متنقل لخدمتك في مكانك.",
                    priceEstimate = "أسعار مناسبة وضمان عمل",
                    isAvailable = true,
                    hasEmergencyService = false,
                    avatarSeed = "ibrahim"
                ),
                CraftsmanEntity(
                    id = 8,
                    name = "سالم المصراتي",
                    craft = "سيراميك وبلاط",
                    experienceYears = 13,
                    phone = "+218919012345",
                    whatsapp = "+218919012345",
                    city = "طرابلس",
                    district = "غوط الشعال",
                    latitude = 32.8660,
                    longitude = 13.1420,
                    bio = "معلم تركيب بلاط، سيراميك ورخام للأرضيات والجدران، استواء تام وتشطيب دقيق للحمامات والمطابخ والصالونات.",
                    priceEstimate = "سعر المتر حسب نوع البلاط",
                    isAvailable = false,
                    hasEmergencyService = false,
                    avatarSeed = "salem"
                )
            )

            dao.insertAllCraftsmen(sampleCraftsmen)

            val sampleReviews = listOf(
                ReviewEntity(
                    craftsmanId = 1,
                    clientName = "عمر الترهوني",
                    qualityRating = 5.0f,
                    punctualityRating = 5.0f,
                    communicationRating = 5.0f,
                    comment = "بارك الله فيك يا أسطى عبد السلام، خدمة نظيفة والتزام بالوقت وشغل متقن في تسريب المياه."
                ),
                ReviewEntity(
                    craftsmanId = 1,
                    clientName = "فاطمة الكيلاني",
                    qualityRating = 4.8f,
                    punctualityRating = 4.7f,
                    communicationRating = 5.0f,
                    comment = "إنسان محترم جداً وسرعة في الحضور وأسعاره طيبة ومناسبة."
                ),
                ReviewEntity(
                    craftsmanId = 2,
                    clientName = "خالد بن عثمان",
                    qualityRating = 5.0f,
                    punctualityRating = 4.8f,
                    communicationRating = 4.9f,
                    comment = "فني كهرباء ممتاز وفاهم شغله، كشف الشورت في دقائق وبارك الله فيه."
                ),
                ReviewEntity(
                    craftsmanId = 2,
                    clientName = "طارق الزروق",
                    qualityRating = 4.9f,
                    punctualityRating = 5.0f,
                    communicationRating = 5.0f,
                    comment = "التزام بالميعاد بالدقيقة وشغله مرتب ونظيف."
                ),
                ReviewEntity(
                    craftsmanId = 3,
                    clientName = "أحمد المجبري",
                    qualityRating = 4.8f,
                    punctualityRating = 4.7f,
                    communicationRating = 4.8f,
                    comment = "صيانة مكيف ممتازة وتبريد ممتاز، أسطى يعتمد عليه في بنغازي."
                ),
                ReviewEntity(
                    craftsmanId = 4,
                    clientName = "وليد القماطي",
                    qualityRating = 5.0f,
                    punctualityRating = 4.6f,
                    communicationRating = 5.0f,
                    comment = "نجار شاطر ركب الدار والمطبخ بإتقان ربي يوفقه."
                )
            )

            dao.insertAllReviews(sampleReviews)

            val samplePortfolioItems = listOf(
                PortfolioItemEntity(
                    craftsmanId = 1,
                    title = "تأسيس شبكة مياه PEX لفيلا طابقين",
                    description = "تنفيذ شبكة مياه معزولة بالكامل مع مجمع توزيع ومضخة ضغط إيطالية واختبار ضغط الأنابيب على 10 بار.",
                    mediaUri = "portfolio_plumbing_1",
                    mediaType = "IMAGE",
                    projectCostLyd = "1,850 د.ل",
                    completedDate = "أكتوبر 2026"
                ),
                PortfolioItemEntity(
                    craftsmanId = 1,
                    title = "صيانة تسريب مخفي وتركيب خلاطات دش مدفونة",
                    description = "كشف التسريب بأجهزة الموجات الصوتية دون تكسير الجدران وتغيير الوصلات مع تركيب دش مطري إيطالي.",
                    mediaUri = "portfolio_plumbing_2",
                    mediaType = "IMAGE",
                    projectCostLyd = "420 د.ل",
                    completedDate = "سبتمبر 2026"
                ),
                PortfolioItemEntity(
                    craftsmanId = 2,
                    title = "لوحة قواطع رئيسية ثلاثية الأطوار مع تأريض كامل",
                    description = "توزيع أحمال الفيلا بدقة، تركيب قواطع شنايدر وتأريض نحاسي لحماية الأجهزة الإلكترونية والمكيفات.",
                    mediaUri = "portfolio_electric_1",
                    mediaType = "IMAGE",
                    projectCostLyd = "1,200 د.ل",
                    completedDate = "أكتوبر 2026"
                ),
                PortfolioItemEntity(
                    craftsmanId = 2,
                    title = "إنارة مخفية LED بروفايل مع تحكم ذكي",
                    description = "تصميم وتنفيذ إضاءة مخفية لصالون ضيوف 40 متر مربع مع ديمر تحكم لاسلكي.",
                    mediaUri = "portfolio_electric_2",
                    mediaType = "IMAGE",
                    projectCostLyd = "650 د.ل",
                    completedDate = "أغسطس 2026"
                ),
                PortfolioItemEntity(
                    craftsmanId = 3,
                    title = "تركيب 8 وحدات تكييف سبليت إنفرتر لمبنى إداري",
                    description = "تمديد مواسير نحاس أصلية معزولة مع شحن غاز R410A وبرمجة ثيرموستات ذكية في بنغازي.",
                    mediaUri = "portfolio_ac_1",
                    mediaType = "IMAGE",
                    projectCostLyd = "2,400 د.ل",
                    completedDate = "أيلول 2026"
                ),
                PortfolioItemEntity(
                    craftsmanId = 4,
                    title = "غرفة نوم ماستر خشب سنديان طبيعي وأبواب سويدي",
                    description = "تفصيل سرير ودولاب خزانة 6 أبواب هيدروليك مع دهان مقاوم للرطوبة.",
                    mediaUri = "portfolio_carpentry_1",
                    mediaType = "IMAGE",
                    projectCostLyd = "4,500 د.ل",
                    completedDate = "سبتمبر 2026"
                )
            )
            dao.insertAllPortfolioItems(samplePortfolioItems)

            val sampleNotification = AppNotificationEntity(
                title = "مرحباً بك في تطبيق حرفي - ليبيا 🇱🇾",
                message = "تطبيق حرفي مجاني وخالٍ تماماً من الإعلانات. يمكنك الوصول السريع لأقرب الحرفيين في مدينتك والتواصل معهم مباشرة أو تسجيل نفسك كحرفي.",
                type = "WELCOME",
                isRead = false
            )
            dao.insertNotification(sampleNotification)
        }
    }
}
