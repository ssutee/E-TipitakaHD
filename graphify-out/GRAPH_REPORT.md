# Graph Report - E-TipitakaHD  (2026-05-13)

## Corpus Check
- 97 files · ~243,857 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1410 nodes · 2278 edges · 63 communities detected
- Extraction: 86% EXTRACTED · 14% INFERRED · 0% AMBIGUOUS · INFERRED: 312 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Community 0|Community 0]]
- [[_COMMUNITY_Community 1|Community 1]]
- [[_COMMUNITY_Community 2|Community 2]]
- [[_COMMUNITY_Community 3|Community 3]]
- [[_COMMUNITY_Community 4|Community 4]]
- [[_COMMUNITY_Community 5|Community 5]]
- [[_COMMUNITY_Community 6|Community 6]]
- [[_COMMUNITY_Community 7|Community 7]]
- [[_COMMUNITY_Community 8|Community 8]]
- [[_COMMUNITY_Community 9|Community 9]]
- [[_COMMUNITY_Community 10|Community 10]]
- [[_COMMUNITY_Community 11|Community 11]]
- [[_COMMUNITY_Community 12|Community 12]]
- [[_COMMUNITY_Community 13|Community 13]]
- [[_COMMUNITY_Community 14|Community 14]]
- [[_COMMUNITY_Community 15|Community 15]]
- [[_COMMUNITY_Community 16|Community 16]]
- [[_COMMUNITY_Community 17|Community 17]]
- [[_COMMUNITY_Community 18|Community 18]]
- [[_COMMUNITY_Community 19|Community 19]]
- [[_COMMUNITY_Community 20|Community 20]]
- [[_COMMUNITY_Community 21|Community 21]]
- [[_COMMUNITY_Community 22|Community 22]]
- [[_COMMUNITY_Community 23|Community 23]]
- [[_COMMUNITY_Community 24|Community 24]]
- [[_COMMUNITY_Community 25|Community 25]]
- [[_COMMUNITY_Community 26|Community 26]]
- [[_COMMUNITY_Community 27|Community 27]]
- [[_COMMUNITY_Community 28|Community 28]]
- [[_COMMUNITY_Community 29|Community 29]]
- [[_COMMUNITY_Community 30|Community 30]]
- [[_COMMUNITY_Community 31|Community 31]]
- [[_COMMUNITY_Community 32|Community 32]]
- [[_COMMUNITY_Community 33|Community 33]]
- [[_COMMUNITY_Community 34|Community 34]]
- [[_COMMUNITY_Community 35|Community 35]]
- [[_COMMUNITY_Community 36|Community 36]]
- [[_COMMUNITY_Community 37|Community 37]]
- [[_COMMUNITY_Community 38|Community 38]]
- [[_COMMUNITY_Community 39|Community 39]]
- [[_COMMUNITY_Community 40|Community 40]]
- [[_COMMUNITY_Community 41|Community 41]]
- [[_COMMUNITY_Community 42|Community 42]]
- [[_COMMUNITY_Community 43|Community 43]]
- [[_COMMUNITY_Community 44|Community 44]]
- [[_COMMUNITY_Community 45|Community 45]]
- [[_COMMUNITY_Community 46|Community 46]]
- [[_COMMUNITY_Community 47|Community 47]]
- [[_COMMUNITY_Community 48|Community 48]]
- [[_COMMUNITY_Community 49|Community 49]]
- [[_COMMUNITY_Community 50|Community 50]]
- [[_COMMUNITY_Community 51|Community 51]]
- [[_COMMUNITY_Community 52|Community 52]]
- [[_COMMUNITY_Community 53|Community 53]]
- [[_COMMUNITY_Community 54|Community 54]]
- [[_COMMUNITY_Community 55|Community 55]]
- [[_COMMUNITY_Community 59|Community 59]]
- [[_COMMUNITY_Community 60|Community 60]]
- [[_COMMUNITY_Community 61|Community 61]]
- [[_COMMUNITY_Community 62|Community 62]]
- [[_COMMUNITY_Community 63|Community 63]]
- [[_COMMUNITY_Community 64|Community 64]]
- [[_COMMUNITY_Community 65|Community 65]]

## God Nodes (most connected - your core abstractions)
1. `MainActivity` - 49 edges
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

## Communities (66 total, 34 thin omitted)

### Community 0 - "Community 0"
Cohesion: 0.03
Nodes (11): BookDatabaseHelper, getCode(), getFullName(), OnGetItemsListener, OnSearchListener, ExternalStorage, DaoHelper, ETHandbookDataModel (+3 more)

### Community 1 - "Community 1"
Cohesion: 0.04
Nodes (92): BookDatabaseHelper, BookListFragment, BookListScreen / BookListScreenBridge, ComparisonActivity, ComparisonActivityContentBridge, ComparisonActivityNavigationModel, ComparisonDivider, Dao (+84 more)

### Community 2 - "Community 2"
Cohesion: 0.03
Nodes (19): ComparisonActivity, AppCompatActivity, DialogFragment, ETDataModel, ETPaliSiamratDataModel, ETThaiMahaChulaDataModel, Fragment, BlankFragment (+11 more)

### Community 3 - "Community 3"
Cohesion: 0.03
Nodes (6): ETHandbookDataModel, OnConvertFromPivotListener, OnConvertToPivotListener, ETThaiFiveBooksDataModel, ETThaiPocketBookDataModel, ETThaiVinayaDataModel

### Community 4 - "Community 4"
Cohesion: 0.05
Nodes (6): CursorPagerAdapter, SearchFragment, FragmentStateAdapter, History, newInstance(), HistoryDaoHelper

### Community 5 - "Community 5"
Cohesion: 0.04
Nodes (37): ComparisonActivityContentBridge, ComparisonDivider(), DictEntryClickListener, DictHeadword, DictScreen(), DictScreenBridge, FileExplorerItemClickListener, FileExplorerPathChangedListener (+29 more)

### Community 6 - "Community 6"
Cohesion: 0.03
Nodes (12): EnglishDictActivity, PaliDictActivity, ThaiDictActivity, EnglishDictAdapter, PaliDictAdapter, ThaiDictAdapter, DictActivity, DictAdapter (+4 more)

### Community 7 - "Community 7"
Cohesion: 0.04
Nodes (6): DaoInstrumentedTest, FavoriteFragment, Favorite, newInstance(), FavoriteDaoHelper, Utils

### Community 9 - "Community 9"
Cohesion: 0.04
Nodes (5): ETBasicDataModel, ETPaliSiamratDataModel, ETThaiMahaChulaDataModel, ETThaiMahaMakutDataModel, ETThaiSiamratDataModel

### Community 10 - "Community 10"
Cohesion: 0.07
Nodes (9): compare(), StartupActivity, getStringCode(), DownloadFileTaskBase, DownloadingFileTask, FileDownloader, OnFileDownloadListener, UnzipUtility (+1 more)

### Community 11 - "Community 11"
Cohesion: 0.08
Nodes (7): ReaderBottomControls(), ReaderChromeBridge, ReaderSeekBar(), ReaderSeekBarListener, ReaderSubtitle(), OnMenuButtonClickListener, ReaderFragment

### Community 12 - "Community 12"
Cohesion: 0.18
Nodes (37): A(), ab(), B(), bb(), C(), D(), E(), F() (+29 more)

### Community 13 - "Community 13"
Cohesion: 0.1
Nodes (29): A(), ab(), B(), Bb(), Cb(), Db(), Eb(), J() (+21 more)

### Community 14 - "Community 14"
Cohesion: 0.08
Nodes (14): DialogAction(), FavoriteActionDialog(), FavoriteActionListener, FavoriteHistoryScreenBridge, FavoriteRow(), FavoriteScreen(), HistoryActionDialog(), HistoryActionListener (+6 more)

### Community 15 - "Community 15"
Cohesion: 0.1
Nodes (24): Bc(), D(), fb(), G(), hb(), I(), ib(), jb() (+16 more)

### Community 17 - "Community 17"
Cohesion: 0.1
Nodes (32): appModule (Koin DI module), BookDatabaseHelper, BookDatabaseHelper.Language, Constants, DaoHelper, DaoInstrumentedTest, DummyContentProvider, ETBasicDataModel (+24 more)

### Community 18 - "Community 18"
Cohesion: 0.1
Nodes (28): activity_main.xml (main activity host layout), Build / IDE Setup (Gradle, JDK 17, API 36), E-TipitakaHD Project (CLAUDE.md), Jetpack Compose Migration Plan (doc), Jetpack Compose Migration Effort, ETipitakaTheme (Compose theme), Compose Migration Ground Rules, Long-Term Cleanup After Compose Adoption (+20 more)

### Community 19 - "Community 19"
Cohesion: 0.14
Nodes (7): DictAdapter, ViewHolder, HeaderViewHolder, SearchResultAdapter, ViewHolder, CursorAdapter, StickyListHeadersAdapter

### Community 20 - "Community 20"
Cohesion: 0.1
Nodes (4): HistoryItem, newInstance(), Status, HistoryItemDaoHelper

### Community 21 - "Community 21"
Cohesion: 0.16
Nodes (23): a(), B(), C(), E(), g(), h(), i(), j() (+15 more)

### Community 22 - "Community 22"
Cohesion: 0.13
Nodes (3): DictActivity, DictDatabaseHelper, SQLiteAssetHelper

### Community 24 - "Community 24"
Cohesion: 0.12
Nodes (3): ContentProvider, DummyContentProvider, DatabaseProvider

### Community 25 - "Community 25"
Cohesion: 0.21
Nodes (16): c(), d(), f(), g(), h(), i(), j(), k() (+8 more)

### Community 27 - "Community 27"
Cohesion: 0.21
Nodes (3): AutoCompleteTextView, ClearableAutoCompleteTextView, Listener

### Community 31 - "Community 31"
Cohesion: 0.43
Nodes (7): download(), downloadDatabaseZipFile(), getCurrentDatabase(), getLocalDatabaseVersion(), isThaiClient(), unzipDatabase(), update()

### Community 39 - "Community 39"
Cohesion: 0.5
Nodes (4): rangy-core.js (vendored Rangy selection/range library core), rangy-cssclassapplier.js (vendored Rangy CSS class applier module), rangy-serializer.js (vendored Rangy selection serializer module), rangy-textrange.js (vendored Rangy text range module)

### Community 40 - "Community 40"
Cohesion: 0.67
Nodes (4): Clear search (X), Menu: font size, Menu: go to (jump arrow), Menu: refresh

### Community 45 - "Community 45"
Cohesion: 0.67
Nodes (3): ClearableAutoCompleteTextView, CursorPagerAdapter, MyWebView

### Community 46 - "Community 46"
Cohesion: 0.67
Nodes (3): app launcher foreground, App launcher icon - blue letter 'E' fused with a Dharma wheel; E-Tipitaka brand mark, THSarabun (SarabunPSK) Thai font family - SVG glyph sheets: Regular, Bold, Italic, BoldItalic; bundled app font for Thai Tipitaka text

## Knowledge Gaps
- **71 isolated node(s):** `Constants`, `DictHeadword`, `ViewHolder`, `ViewHolder`, `HeaderViewHolder` (+66 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **34 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Utils` connect `Community 2` to `Community 0`, `Community 1`, `Community 3`, `Community 9`, `Community 11`, `Community 19`?**
  _High betweenness centrality (0.169) - this node is a cross-community bridge._
- **Why does `FavoriteFragment` connect `Community 7` to `Community 8`, `Community 2`?**
  _High betweenness centrality (0.060) - this node is a cross-community bridge._
- **Why does `SearchFragment` connect `Community 4` to `Community 2`?**
  _High betweenness centrality (0.050) - this node is a cross-community bridge._
- **What connects `Constants`, `DictHeadword`, `ViewHolder` to the rest of the system?**
  _71 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 0` be split into smaller, more focused modules?**
  _Cohesion score 0.03 - nodes in this community are weakly interconnected._
- **Should `Community 1` be split into smaller, more focused modules?**
  _Cohesion score 0.04 - nodes in this community are weakly interconnected._
- **Should `Community 2` be split into smaller, more focused modules?**
  _Cohesion score 0.03 - nodes in this community are weakly interconnected._