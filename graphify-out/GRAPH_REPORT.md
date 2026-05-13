# Graph Report - E-TipitakaHD (Android Tipitaka reader)  (2026-05-13)

## Corpus Check
- Large corpus: 221 files · ~243,317 words. Semantic extraction will be expensive (many Claude tokens). Consider running on a subfolder, or use --no-semantic to run AST-only.

## Summary
- 1405 nodes · 2268 edges · 61 communities detected
- Extraction: 86% EXTRACTED · 14% INFERRED · 0% AMBIGUOUS · INFERRED: 310 edges (avg confidence: 0.8)
- Token cost: 327,882 input · 81,978 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Book Database & Data Access|Book Database & Data Access]]
- [[_COMMUNITY_Comparison & Dictionary Core|Comparison & Dictionary Core]]
- [[_COMMUNITY_Activity Lifecycle & Pali Editions|Activity Lifecycle & Pali Editions]]
- [[_COMMUNITY_Compose UI Bridges & Renderers|Compose UI Bridges & Renderers]]
- [[_COMMUNITY_Thai Data Models & Pivot Conversion|Thai Data Models & Pivot Conversion]]
- [[_COMMUNITY_Search & History Paging|Search & History Paging]]
- [[_COMMUNITY_Dictionary Activities & Fonts|Dictionary Activities & Fonts]]
- [[_COMMUNITY_App Startup & Data Import|App Startup & Data Import]]
- [[_COMMUNITY_Siamrat & MahaChula Editions|Siamrat & MahaChula Editions]]
- [[_COMMUNITY_Main Activity Orchestration|Main Activity Orchestration]]
- [[_COMMUNITY_Favorites|Favorites]]
- [[_COMMUNITY_File Explorer & Downloads|File Explorer & Downloads]]
- [[_COMMUNITY_History Fragment & Shared State|History Fragment & Shared State]]
- [[_COMMUNITY_Rangy Core (vendored)|Rangy Core (vendored)]]
- [[_COMMUNITY_Reader UI & Chrome|Reader UI & Chrome]]
- [[_COMMUNITY_Rangy TextRange (vendored)|Rangy TextRange (vendored)]]
- [[_COMMUNITY_jQuery (vendored)|jQuery (vendored)]]
- [[_COMMUNITY_ETDataModel Base Class|ETDataModel Base Class]]
- [[_COMMUNITY_Dependency Injection & App Wiring|Dependency Injection & App Wiring]]
- [[_COMMUNITY_HistoryItem Persistence|HistoryItem Persistence]]
- [[_COMMUNITY_Compose Migration Plan (docs)|Compose Migration Plan (docs)]]
- [[_COMMUNITY_Rangy CSS Class Applier (vendored)|Rangy CSS Class Applier (vendored)]]
- [[_COMMUNITY_Generic Dictionary Screen|Generic Dictionary Screen]]
- [[_COMMUNITY_Content Providers|Content Providers]]
- [[_COMMUNITY_Rangy Serializer (vendored)|Rangy Serializer (vendored)]]
- [[_COMMUNITY_ETBasicDataModel|ETBasicDataModel]]
- [[_COMMUNITY_Clearable AutoComplete Widget|Clearable AutoComplete Widget]]
- [[_COMMUNITY_Thai Supreme Edition Model|Thai Supreme Edition Model]]
- [[_COMMUNITY_Generic DAO|Generic DAO]]
- [[_COMMUNITY_Database Download|Database Download]]
- [[_COMMUNITY_Storage Utilities|Storage Utilities]]
- [[_COMMUNITY_SQLite Open Helper|SQLite Open Helper]]
- [[_COMMUNITY_Model Base (Kotlin)|Model Base (Kotlin)]]
- [[_COMMUNITY_Favorite Table Schema|Favorite Table Schema]]
- [[_COMMUNITY_HistoryItem Table Schema|HistoryItem Table Schema]]
- [[_COMMUNITY_History Table Schema|History Table Schema]]
- [[_COMMUNITY_Reader Fragment Host|Reader Fragment Host]]
- [[_COMMUNITY_Rangy Library Modules|Rangy Library Modules]]
- [[_COMMUNITY_Toolbar Menu Icons|Toolbar Menu Icons]]
- [[_COMMUNITY_Instrumented Test Sample|Instrumented Test Sample]]
- [[_COMMUNITY_Application Class|Application Class]]
- [[_COMMUNITY_Data Model Factory|Data Model Factory]]
- [[_COMMUNITY_Unit Test Sample|Unit Test Sample]]
- [[_COMMUNITY_Custom View Widgets|Custom View Widgets]]
- [[_COMMUNITY_App Brand Assets|App Brand Assets]]
- [[_COMMUNITY_Constants|Constants]]
- [[_COMMUNITY_Comparison Navigation Model|Comparison Navigation Model]]
- [[_COMMUNITY_File Explorer Navigation Model|File Explorer Navigation Model]]
- [[_COMMUNITY_BookMenu Drawer Icons|Book/Menu Drawer Icons]]
- [[_COMMUNITY_Holo Checkbox Drawables|Holo Checkbox Drawables]]
- [[_COMMUNITY_Checkbox State Drawables|Checkbox State Drawables]]
- [[_COMMUNITY_Checkbox & Star Icons|Checkbox & Star Icons]]
- [[_COMMUNITY_Checkbox OnOff Icons|Checkbox On/Off Icons]]
- [[_COMMUNITY_File & Folder Icons|File & Folder Icons]]
- [[_COMMUNITY_Instrumented Test (dup)|Instrumented Test (dup)]]
- [[_COMMUNITY_Unit Test (dup)|Unit Test (dup)]]
- [[_COMMUNITY_jQuery File Node|jQuery File Node]]
- [[_COMMUNITY_Blank Fragment|Blank Fragment]]
- [[_COMMUNITY_File Explorer Nav Model (dup)|File Explorer Nav Model (dup)]]
- [[_COMMUNITY_Rating Star Drawable|Rating Star Drawable]]
- [[_COMMUNITY_Rating Star Icon (dup)|Rating Star Icon (dup)]]

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 47 edges
2. `BookDatabaseHelper` - 36 edges
3. `ETDataModel` - 33 edges
4. `ReaderFragment` - 30 edges
5. `History` - 30 edges
6. `getCode()` - 27 edges
7. `Utils` - 27 edges
8. `ETThaiFiveBooksDataModel` - 26 edges
9. `ETSiamratDataModel` - 23 edges
10. `ETThaiWatnaDataModel` - 23 edges

## Surprising Connections (you probably didn't know these)
- `Phase 14: Full Screen Conversions` --conceptually_related_to--> `Bookmarks / History Feature`  [INFERRED]
  docs/COMPOSE_MIGRATION_PLAN.md → README.md
- `Reader High-Risk Component (ViewPager2 + PageFragment + MyWebView)` --conceptually_related_to--> `Highlight Important Text Feature`  [INFERRED]
  docs/COMPOSE_MIGRATION_PLAN.md → CLAUDE.md
- `Reader High-Risk Component (ViewPager2 + PageFragment + MyWebView)` --conceptually_related_to--> `Reader Controls (volume/page navigation)`  [INFERRED]
  docs/COMPOSE_MIGRATION_PLAN.md → README.md
- `ComparisonActivity` --semantically_similar_to--> `ComparisonActivityContentBridge`  [INFERRED] [semantically similar]
  app/src/main/java/com/watnapp/etipitaka/plus/activity/ComparisonActivity.java → app/src/main/java/com/watnapp/etipitaka/plus/activity/ComparisonActivityHost.kt
- `ETHandbookDataModel` --semantically_similar_to--> `ETDataModel`  [INFERRED] [semantically similar]
  app/src/main/java/com/watnapp/etipitaka/plus/model/ETHandbookDataModel.java → app/src/main/java/com/watnapp/etipitaka/plus/model/ETDataModel.java

## Hyperedges (group relationships)
- **Koin dependency injection bootstrap** — etipitakaapplication_class, appmodule_kt, sharedviewmodel_class [INFERRED 0.85]
- **Rangy text-highlight asset stack** — rangy_corejs, rangy_cssclassapplierjs, rangy_serializerjs, rangy_textrangejs [INFERRED 0.85]
- **Tipitaka database path/language resolution** — utils_class, bookdatabasehelper_language, constants_class [INFERRED 0.75]
- **Pali-Thai-English dictionary activities** — dict_activity, pali_dict_activity, thai_dict_activity, english_dict_activity [INFERRED 0.85]
- **Java activity to Compose screen migration pairs** — dict_screen, file_explorer_screen, main_activity_host, comparison_activity_content_bridge [INFERRED 0.75]
- **Tipitaka reading and comparison navigation flow** — startup_activity, main_activity, comparison_activity, reader_fragment [INFERRED 0.75]
- **Dictionary list adapters** — dict_adapter, english_dict_adapter, pali_dict_adapter, thai_dict_adapter [INFERRED 0.95]
- **Menu tab fragments** — menu_fragment, book_list_fragment, history_fragment, favorite_fragment [INFERRED 0.85]
- **Reader/list Compose screen bridges** — book_list_screen, favorite_history_screen, menu_tabs, reader_chrome [INFERRED 0.75]
- **Dictionary database helper family** — dict_database_helper, english_dict_database_helper, pali_dict_database_helper, thai_dict_database_helper [INFERRED 0.95]
- **Database download/update pipeline** — download_database, file_downloader, unzip_utility [INFERRED 0.85]
- **Reader screen composition** — reader_fragment, reader_fragment_content_bridge, reader_chrome_bridge [INFERRED 0.75]
- **Tipitaka edition data models** — et_data_model, et_basic_data_model, et_pali_siamrat_data_model, et_pali_siamrat_new_data_model, et_roman_script_data_model, et_siamrat_data_model, et_thai_five_books_data_model, et_thai_maha_chula_data_model, et_thai_maha_chula2_data_model, et_handbook_data_model [INFERRED 0.85]
- **ETDataModel factory creation** — et_data_model_creator, et_data_model, et_pali_siamrat_data_model, et_thai_maha_chula_data_model, et_roman_script_data_model, et_thai_five_books_data_model [INFERRED 0.85]
- **DAO/database access layer** — dao, dao_helper, model_base, database_provider, database_open_helper, history_table, favorite_table, history_item_table [INFERRED 0.85]
- **Favorites persistence triad** — favorite_class, favoritetable_object, favoritedaohelper_class [INFERRED 0.85]
- **History and HistoryItem persistence** — history_class, historytable_object, historydaohelper_class, historyitem_class, historyitemtable_object, historyitemdaohelper_class [INFERRED 0.80]
- **Thai-edition data models** — etthaimahamakutdatamodel_class, etthaipocketbookdatamodel_class, etthaisiamratdatamodel_class, etthaisupremedatamodel_class, etthaivinayadatamodel_class, etthaiwatnadatamodel_class [INFERRED 0.85]
- **Compose migration plan phases** — composemigrationplan_phase7foundation, composemigrationplan_phase8themebridge, composemigrationplan_phase9dialogs, composemigrationplan_phase10listrows, composemigrationplan_phase11sidemenu, composemigrationplan_phase12readerchrome, composemigrationplan_phase13mainhost, composemigrationplan_phase14fullscreens, composemigrationplan_longtermcleanup [EXTRACTED 1.00]
- **Reader-helper feature set** — feature_palithaidictionary, feature_tipitakacomparison, feature_highlight, feature_bookmarkshistory, feature_readercontrols [INFERRED 0.85]
- **Compose migration code targets** — dictscreen_dictscreen, activitymain_activitymainxml, pagefragment_pagefragment, mywebview_mywebview, slidingmenu_slidingmenu [INFERRED 0.75]
- **Thai SarabunPSK font family (Regular/Bold/Italic/BoldItalic)** — thsarabun_font_family, app_launcher_icon [INFERRED 0.65]
- **Holo Light checkbox state drawable set (on/off variants)** — checkbox_off_holo_light, checkbox_on_holo_light [INFERRED 0.95]
- **Menu book/open toolbar navigation icons** — icon_menu_book, icon_menu_open [INFERRED 0.85]
- **Checkbox state drawable set** — icon_checkbox_checked, icon_checkbox_unchecked [INFERRED 0.95]
- **Toolbar/options menu icons** — icon_menu_font, icon_menu_goto, icon_menu_refresh, icon_menu_book, icon_menu_open [INFERRED 0.85]
- **Action bar / options menu icons** — icon_menu_font, icon_menu_goto, icon_menu_refresh [INFERRED 0.75]
- **Reading-view book icons** — icon_menu_book, icon_menu_open [INFERRED 0.75]
- **Options Menu Action Icons** — icon_menu_font, icon_menu_goto, icon_menu_refresh [INFERRED 0.75]
- **File Explorer Item Icons** — icon_file, icon_folder [INFERRED 0.85]
- **app launcher icon set** — app_launcher_icon, app_launcher_foreground [INFERRED 0.95]
- **toolbar menu icons** — icon_menu_book, icon_menu_open [INFERRED 0.85]

## Communities (64 total, 32 thin omitted)

### Community 0 - "Book Database & Data Access"
Cohesion: 0.03
Nodes (10): BookDatabaseHelper, getCode(), OnGetItemsListener, OnSearchListener, ExternalStorage, DaoHelper, ETHandbookDataModel, ETRomanScriptDataModel (+2 more)

### Community 1 - "Comparison & Dictionary Core"
Cohesion: 0.04
Nodes (92): BookDatabaseHelper, BookListFragment, BookListScreen / BookListScreenBridge, ComparisonActivity, ComparisonActivityContentBridge, ComparisonActivityNavigationModel, ComparisonDivider, Dao (+84 more)

### Community 2 - "Activity Lifecycle & Pali Editions"
Cohesion: 0.03
Nodes (19): ComparisonActivity, AppCompatActivity, DialogFragment, ETDataModel, ETPaliSiamratDataModel, ETThaiMahaChulaDataModel, Fragment, BlankFragment (+11 more)

### Community 3 - "Compose UI Bridges & Renderers"
Cohesion: 0.04
Nodes (48): ComparisonActivityContentBridge, ComparisonDivider(), DictEntryClickListener, DictHeadword, DictScreen(), DictScreenBridge, FileExplorerItemClickListener, FileExplorerPathChangedListener (+40 more)

### Community 4 - "Thai Data Models & Pivot Conversion"
Cohesion: 0.03
Nodes (6): ETHandbookDataModel, OnConvertFromPivotListener, OnConvertToPivotListener, ETThaiFiveBooksDataModel, ETThaiPocketBookDataModel, ETThaiVinayaDataModel

### Community 5 - "Search & History Paging"
Cohesion: 0.05
Nodes (6): CursorPagerAdapter, SearchFragment, FragmentStateAdapter, History, newInstance(), HistoryDaoHelper

### Community 6 - "Dictionary Activities & Fonts"
Cohesion: 0.03
Nodes (12): EnglishDictActivity, PaliDictActivity, ThaiDictActivity, EnglishDictAdapter, PaliDictAdapter, ThaiDictAdapter, DictActivity, DictAdapter (+4 more)

### Community 7 - "App Startup & Data Import"
Cohesion: 0.06
Nodes (12): StartupActivity, DictAdapter, ViewHolder, HeaderViewHolder, SearchResultAdapter, ViewHolder, CursorAdapter, getFullName() (+4 more)

### Community 8 - "Siamrat & MahaChula Editions"
Cohesion: 0.04
Nodes (5): ETBasicDataModel, ETPaliSiamratDataModel, ETThaiMahaChulaDataModel, ETThaiMahaMakutDataModel, ETThaiSiamratDataModel

### Community 10 - "Favorites"
Cohesion: 0.06
Nodes (4): FavoriteFragment, Favorite, newInstance(), FavoriteDaoHelper

### Community 11 - "File Explorer & Downloads"
Cohesion: 0.08
Nodes (8): compare(), FileExplorerActivity, ComponentActivity, DownloadFileTaskBase, DownloadingFileTask, FileDownloader, OnFileDownloadListener, Runnable

### Community 12 - "History Fragment & Shared State"
Cohesion: 0.07
Nodes (4): HistoryFragment, OnHistorySelectedListener, MenuFragment, SharedViewModel

### Community 13 - "Rangy Core (vendored)"
Cohesion: 0.18
Nodes (37): A(), ab(), B(), bb(), C(), D(), E(), F() (+29 more)

### Community 14 - "Reader UI & Chrome"
Cohesion: 0.09
Nodes (7): ReaderBottomControls(), ReaderChromeBridge, ReaderSeekBar(), ReaderSeekBarListener, ReaderSubtitle(), OnMenuButtonClickListener, ReaderFragment

### Community 15 - "Rangy TextRange (vendored)"
Cohesion: 0.1
Nodes (29): A(), ab(), B(), Bb(), Cb(), Db(), Eb(), J() (+21 more)

### Community 16 - "jQuery (vendored)"
Cohesion: 0.1
Nodes (24): Bc(), D(), fb(), G(), hb(), I(), ib(), jb() (+16 more)

### Community 18 - "Dependency Injection & App Wiring"
Cohesion: 0.1
Nodes (32): appModule (Koin DI module), BookDatabaseHelper, BookDatabaseHelper.Language, Constants, DaoHelper, DaoInstrumentedTest, DummyContentProvider, ETBasicDataModel (+24 more)

### Community 19 - "HistoryItem Persistence"
Cohesion: 0.09
Nodes (5): DaoInstrumentedTest, HistoryItem, newInstance(), Status, HistoryItemDaoHelper

### Community 20 - "Compose Migration Plan (docs)"
Cohesion: 0.1
Nodes (28): activity_main.xml (main activity host layout), Build / IDE Setup (Gradle, JDK 17, API 36), E-TipitakaHD Project (CLAUDE.md), Jetpack Compose Migration Plan (doc), Jetpack Compose Migration Effort, ETipitakaTheme (Compose theme), Compose Migration Ground Rules, Long-Term Cleanup After Compose Adoption (+20 more)

### Community 21 - "Rangy CSS Class Applier (vendored)"
Cohesion: 0.16
Nodes (23): a(), B(), C(), E(), g(), h(), i(), j() (+15 more)

### Community 22 - "Generic Dictionary Screen"
Cohesion: 0.13
Nodes (3): DictActivity, DictDatabaseHelper, SQLiteAssetHelper

### Community 23 - "Content Providers"
Cohesion: 0.12
Nodes (3): ContentProvider, DummyContentProvider, DatabaseProvider

### Community 24 - "Rangy Serializer (vendored)"
Cohesion: 0.21
Nodes (16): c(), d(), f(), g(), h(), i(), j(), k() (+8 more)

### Community 26 - "Clearable AutoComplete Widget"
Cohesion: 0.21
Nodes (3): AutoCompleteTextView, ClearableAutoCompleteTextView, Listener

### Community 29 - "Database Download"
Cohesion: 0.43
Nodes (7): download(), downloadDatabaseZipFile(), getCurrentDatabase(), getLocalDatabaseVersion(), isThaiClient(), unzipDatabase(), update()

### Community 37 - "Rangy Library Modules"
Cohesion: 0.5
Nodes (4): rangy-core.js (vendored Rangy selection/range library core), rangy-cssclassapplier.js (vendored Rangy CSS class applier module), rangy-serializer.js (vendored Rangy selection serializer module), rangy-textrange.js (vendored Rangy text range module)

### Community 38 - "Toolbar Menu Icons"
Cohesion: 0.67
Nodes (4): Clear search (X), Menu: font size, Menu: go to (jump arrow), Menu: refresh

### Community 43 - "Custom View Widgets"
Cohesion: 0.67
Nodes (3): ClearableAutoCompleteTextView, CursorPagerAdapter, MyWebView

### Community 44 - "App Brand Assets"
Cohesion: 0.67
Nodes (3): app launcher foreground, App launcher icon - blue letter 'E' fused with a Dharma wheel; E-Tipitaka brand mark, THSarabun (SarabunPSK) Thai font family - SVG glyph sheets: Regular, Bold, Italic, BoldItalic; bundled app font for Thai Tipitaka text

## Knowledge Gaps
- **71 isolated node(s):** `Constants`, `DictHeadword`, `ViewHolder`, `ViewHolder`, `HeaderViewHolder` (+66 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **32 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Utils` connect `Activity Lifecycle & Pali Editions` to `Book Database & Data Access`, `Comparison & Dictionary Core`, `Thai Data Models & Pivot Conversion`, `App Startup & Data Import`, `Siamrat & MahaChula Editions`, `Reader UI & Chrome`?**
  _High betweenness centrality (0.166) - this node is a cross-community bridge._
- **Why does `FavoriteFragment` connect `Favorites` to `Main Activity Orchestration`, `Activity Lifecycle & Pali Editions`?**
  _High betweenness centrality (0.056) - this node is a cross-community bridge._
- **Why does `ReaderFragment` connect `Reader UI & Chrome` to `Book Database & Data Access`, `Main Activity Orchestration`, `Activity Lifecycle & Pali Editions`?**
  _High betweenness centrality (0.054) - this node is a cross-community bridge._
- **What connects `Constants`, `DictHeadword`, `ViewHolder` to the rest of the system?**
  _71 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Book Database & Data Access` be split into smaller, more focused modules?**
  _Cohesion score 0.03 - nodes in this community are weakly interconnected._
- **Should `Comparison & Dictionary Core` be split into smaller, more focused modules?**
  _Cohesion score 0.04 - nodes in this community are weakly interconnected._
- **Should `Activity Lifecycle & Pali Editions` be split into smaller, more focused modules?**
  _Cohesion score 0.03 - nodes in this community are weakly interconnected._