package com.mas.alsalah

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Forest = Color(0xFF174C3B)
private val Mint = Color(0xFFB9D8C8)
private val Ink = Color(0xFF17231F)
private val Muted = Color(0xFF7C8982)
private val Canvas = Color(0xFFF3F6F3)
private val Glass = Color(0xDFFFFFFF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(243, 246, 243)
        window.navigationBarColor = android.graphics.Color.rgb(243, 246, 243)
        setContent { AlSalahApp() }
    }
}

private enum class Tab(val title: String, val icon: ImageVector) {
    HOME("الرئيسية", Icons.Outlined.Home),
    PRAYER("الصلاة", Icons.Outlined.AccessTime),
    QIBLA("القبلة", Icons.Outlined.Explore),
    ADHKAR("الأذكار", Icons.Outlined.AutoStories),
    SETTINGS("المزيد", Icons.Outlined.GridView)
}

@Composable
private fun AlSalahApp() {
    var selected by remember { mutableStateOf(Tab.HOME) }
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Forest, onPrimary = Color.White,
            background = Canvas, surface = Color.White,
            onSurface = Ink, secondary = Color(0xFF8DB8A2)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color(0xFFEAF2EC), Canvas, Color(0xFFF8F9F7)))
            )
        ) {
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f)) {
                    when (selected) {
                        Tab.HOME -> HomeScreen(onOpenPrayer = { selected = Tab.PRAYER }, onOpenAdhkar = { selected = Tab.ADHKAR })
                        Tab.PRAYER -> PrayerScreen()
                        Tab.QIBLA -> QiblaScreen()
                        Tab.ADHKAR -> AdhkarScreen()
                        Tab.SETTINGS -> MoreScreen()
                    }
                }
                GlassBottomBar(selected = selected, onSelect = { selected = it })
            }
        }
    }
}

@Composable
private fun Page(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 18.dp),
        horizontalAlignment = Alignment.End,
        content = content
    )
}

@Composable
private fun Header(kicker: String = "AL-SALAH  •  BY MAS", title: String, subtitle: String? = null) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = Color.White.copy(.72f), border = BorderStroke(1.dp, Color.White)) {
            Icon(Icons.Outlined.NotificationsNone, null, tint = Forest, modifier = Modifier.padding(12.dp))
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(kicker, color = Forest, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Text(title, color = Ink, fontSize = 27.sp, fontWeight = FontWeight.Bold)
            if (subtitle != null) Text(subtitle, color = Muted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(26.dp),
        color = Glass,
        border = BorderStroke(1.dp, Color.White.copy(.95f)),
        shadowElevation = 8.dp
    ) {
        Column(Modifier.padding(18.dp), content = content)
    }
}

@Composable
private fun HomeScreen(onOpenPrayer: () -> Unit, onOpenAdhkar: () -> Unit) {
    Page {
        Header(title = "السلام عليك", subtitle = "اجعل صلاتك نور يومك")
        Spacer(Modifier.height(22.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            color = Forest,
            shadowElevation = 12.dp
        ) {
            Box {
                Box(Modifier.size(170.dp).offset(x = (-40).dp, y = (-35).dp).clip(CircleShape).background(Color.White.copy(.055f)))
                Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.End) {
                    Text("مواقيت الصلاة", color = Mint, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("حيّ على الصلاة", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Text("فعّل موقعك لعرض المواقيت الدقيقة", color = Color.White.copy(.78f), fontSize = 13.sp)
                    Spacer(Modifier.height(18.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onOpenPrayer, colors = ButtonDefaults.textButtonColors(contentColor = Color.White)) {
                            Text("عرض المواقيت  ←")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.LocationOn, null, tint = Mint, modifier = Modifier.size(16.dp))
                            Text("الموقع غير محدد", color = Mint, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(22.dp))
        SectionTitle("صلوات اليوم", action = "عرض الكل", onAction = onOpenPrayer)
        Spacer(Modifier.height(12.dp))
        GlassCard(Modifier.fillMaxWidth()) {
            val prayers = listOf("الفجر", "الشروق", "الظهر", "العصر", "المغرب", "العشاء")
            prayers.forEachIndexed { index, name ->
                Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("— —", color = Muted, fontSize = 14.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(name, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Spacer(Modifier.width(10.dp))
                        Icon(if (index == 0) Icons.Outlined.WbTwilight else Icons.Outlined.Mosque, null, tint = Forest, modifier = Modifier.size(18.dp))
                    }
                }
                if (index != prayers.lastIndex) HorizontalDivider(color = Color(0xFFE9EEEA))
            }
            Text("الأوقات ستظهر بعد تحديد الموقع وطريقة الحساب", color = Muted, fontSize = 10.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(22.dp))
        SectionTitle("مساحتك الإيمانية")
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickTile("الأذكار", "حصنك اليومي", Icons.Outlined.AutoStories, Modifier.weight(1f), onOpenAdhkar)
            QuickTile("القبلة", "اتجاه الكعبة", Icons.Outlined.Explore, Modifier.weight(1f)) {}
        }
        Spacer(Modifier.height(12.dp))
        GlassCard(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.MenuBook, null, tint = Forest, modifier = Modifier.size(30.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text("القرآن الكريم", color = Ink, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("كن قريباً من وردك كل يوم", color = Muted, fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("﴿ أَقِمِ الصَّلَاةَ لِذِكْرِي ﴾", color = Forest, fontSize = 18.sp, fontWeight = FontWeight.Medium, modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp), textAlign = TextAlign.Center)
    }
}

@Composable
private fun PrayerScreen() {
    Page {
        Header(title = "مواقيت الصلاة", subtitle = "صلاتك في وقتها سكينة لقلبك")
        Spacer(Modifier.height(22.dp))
        GlassCard(Modifier.fillMaxWidth()) {
            Text("التاريخ الهجري", color = Muted, fontSize = 12.sp, modifier = Modifier.align(Alignment.End))
            Text("سيظهر بعد تفعيل التقويم", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.End))
            Spacer(Modifier.height(8.dp))
            Text("حدد موقعك وطريقة الحساب لعرض جدول دقيق دون تخمين.", color = Muted, fontSize = 12.sp, modifier = Modifier.align(Alignment.End))
        }
        Spacer(Modifier.height(16.dp))
        listOf("الفجر" to Icons.Outlined.WbTwilight, "الشروق" to Icons.Outlined.WbSunny, "الظهر" to Icons.Outlined.LightMode, "العصر" to Icons.Outlined.WbCloudy, "المغرب" to Icons.Outlined.WbTwilight, "العشاء" to Icons.Outlined.DarkMode).forEach { (name, icon) ->
            GlassCard(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("--:--", color = Muted, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(name, color = Ink, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(12.dp))
                        Icon(icon, null, tint = Forest)
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("إعدادات الحساب والتنبيه ستكون متاحة عند استكمال ربط الموقع ومحرك المواقيت.", color = Muted, fontSize = 11.sp)
    }
}

@Composable
private fun QiblaScreen() {
    Page {
        Header(title = "اتجاه القبلة", subtitle = "وجه قلبك نحو الكعبة")
        Spacer(Modifier.height(24.dp))
        GlassCard(Modifier.fillMaxWidth()) {
            Box(Modifier.fillMaxWidth().height(260.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.size(220.dp).clip(CircleShape).background(Brush.radialGradient(listOf(Color.White, Color(0xFFE1EEE6)))).then(Modifier), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(180.dp).clip(CircleShape).background(Color.Transparent).padding(8.dp), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Explore, null, tint = Forest, modifier = Modifier.size(112.dp))
                    }
                }
                Column(Modifier.align(Alignment.BottomCenter), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("البوصلة غير مفعّلة بعد", color = Ink, fontWeight = FontWeight.Bold)
                    Text("اسمح باستخدام مستشعرات الجهاز لتحديد الاتجاه", color = Muted, fontSize = 11.sp)
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        GlassCard(Modifier.fillMaxWidth()) {
            Text("ملاحظة", color = Forest, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.End))
            Text("دقة اتجاه القبلة تعتمد على مستشعرات جهازك ومعايرتها، وسيتم تفعيل البوصلة الحية في مرحلة ربط المستشعرات.", color = Muted, fontSize = 12.sp, textAlign = TextAlign.End)
        }
    }
}

@Composable
private fun AdhkarScreen() {
    var count by remember { mutableIntStateOf(0) }
    Page {
        Header(title = "الأذكار", subtitle = "ألا بذكر الله تطمئن القلوب")
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("الصباح", "المساء", "بعد الصلاة").forEach { label ->
                Surface(shape = RoundedCornerShape(18.dp), color = Color.White.copy(.8f), border = BorderStroke(1.dp, Color.White)) {
                    Text(label, modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), color = Forest, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        GlassCard(Modifier.fillMaxWidth()) {
            Text("سُبْحَانَ اللَّهِ وَبِحَمْدِهِ", color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 22.dp))
            Text("عدد التسبيحات", color = Muted, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
            Text("$count", color = Forest, fontSize = 42.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(12.dp))
            Button(onClick = { count++ }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp)) {
                Text("تسبيحة جديدة", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = { count = 0 }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("إعادة العداد") }
        }
        Spacer(Modifier.height(16.dp))
        GlassCard(Modifier.fillMaxWidth()) {
            Text("أذكار مختارة", color = Ink, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.End))
            Spacer(Modifier.height(10.dp))
            Text("أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ", color = Forest, fontSize = 18.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            HorizontalDivider(Modifier.padding(vertical = 14.dp), color = Color(0xFFE9EEEA))
            Text("اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّد", color = Forest, fontSize = 18.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun MoreScreen() {
    Page {
        Header(title = "المزيد", subtitle = "خصص تجربتك في الصلاة")
        Spacer(Modifier.height(22.dp))
        listOf(
            Triple("القرآن الكريم", "المصحف والورد اليومي", Icons.Outlined.MenuBook),
            Triple("التقويم الهجري", "المناسبات والتواريخ", Icons.Outlined.CalendarMonth),
            Triple("الأدعية", "أدعية من القرآن والسنة", Icons.Outlined.FavoriteBorder),
            Triple("التنبيهات", "إعداد أوقات التذكير", Icons.Outlined.NotificationsActive),
            Triple("إعدادات الموقع", "تحديد المدينة وطريقة الحساب", Icons.Outlined.LocationOn),
            Triple("عن التطبيق", "الصلاة — AL-SALAH by MAS", Icons.Outlined.Info)
        ).forEach { (title, subtitle, icon) ->
            GlassCard(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.ChevronLeft, null, tint = Muted)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(title, color = Ink, fontWeight = FontWeight.Bold)
                        Text(subtitle, color = Muted, fontSize = 11.sp)
                    }
                    Icon(icon, null, tint = Forest)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        if (action != null && onAction != null) TextButton(onClick = onAction, contentPadding = PaddingValues(0.dp)) { Text(action, color = Forest, fontSize = 11.sp) }
        else Spacer(Modifier.width(1.dp))
        Text(title, color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun QuickTile(title: String, subtitle: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(24.dp), color = Glass, border = BorderStroke(1.dp, Color.White), shadowElevation = 6.dp) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.End) {
            Icon(icon, null, tint = Forest, modifier = Modifier.size(26.dp))
            Spacer(Modifier.height(12.dp))
            Text(title, color = Ink, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun GlassBottomBar(selected: Tab, onSelect: (Tab) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 12.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color(0xEFFFFFFF),
        border = BorderStroke(1.dp, Color.White),
        shadowElevation = 18.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 5.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Tab.values().forEach { tab ->
                val active = selected == tab
                Surface(
                    onClick = { onSelect(tab) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (active) Color(0xFFE4F0E8) else Color.Transparent
                ) {
                    Column(
                        modifier = Modifier.widthIn(min = 54.dp).padding(horizontal = 9.dp, vertical = 7.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(tab.icon, contentDescription = tab.title, tint = if (active) Forest else Muted, modifier = Modifier.size(21.dp))
                        Spacer(Modifier.height(3.dp))
                        Text(tab.title, color = if (active) Forest else Muted, fontSize = 9.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }
    }
}
