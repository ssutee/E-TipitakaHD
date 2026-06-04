package com.watnapp.etipitaka;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.watnapp.etipitaka.plus.model.ETThaiMahaChula2DataModel;
import com.watnapp.etipitaka.plus.model.ETThaiMahaChulaDataModel;
import com.watnapp.etipitaka.plus.model.ETThaiSupremeDataModel;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Regression tests for the differing-index compare/jump fix.
 *
 * <p>The MC / MS item-mapping tables (mc_map.json / ms_map.json bundled in
 * assets) have scattered gaps. Comparing or jumping by default (Siam)
 * indexing into Maha Chula (thaimc / thaimc2) or Maha Supreme (thaims) used
 * to return page 0 when the exact {@code v{vol}-{section}-i{item}} key was
 * missing. The fix walks down to the nearest lower item that exists.
 *
 * <p>Mirrors the iOS CompareFromPivotTests and the PC
 * tests/test_compare_mapping.py. These touch only the convert-item map
 * (loaded from assets), so no language database file is required.
 *
 * <p>Known gap data verified directly against the asset JSON:
 * <ul>
 *   <li>mc_map v37-1-i123 -&gt; page 69, i124 absent, i125 -&gt; page 70</li>
 *   <li>ms_map v37-1-i31 -&gt; page 30, i32/i33 absent, i34 -&gt; page 32</li>
 * </ul>
 */
@RunWith(AndroidJUnit4.class)
public class CompareNearestItemInstrumentedTest {

  private Context context() {
    return InstrumentationRegistry.getInstrumentation().getTargetContext();
  }

  // ── Maha Chula (thaimc) ────────────────────────────────────────────────

  @Test
  public void mahaChula_presentItem_unchanged() {
    ETThaiMahaChulaDataModel model = new ETThaiMahaChulaDataModel(context());
    // i123 and i125 are present in the map; their pages must not change.
    assertEquals(69, model.getPageByItem(37, 123, 1, true));
    assertEquals(70, model.getPageByItem(37, 125, 1, true));
  }

  @Test
  public void mahaChula_missingItem_fallsBackToNearestLower() {
    ETThaiMahaChulaDataModel model = new ETThaiMahaChulaDataModel(context());
    // i124 is absent from the map; nearest lower present item is i123 (page 69).
    assertEquals(69, model.getPageByItem(37, 124, 1, true));
  }

  @Test
  public void mahaChula_missingItem_doesNotReturnZero() {
    ETThaiMahaChulaDataModel model = new ETThaiMahaChulaDataModel(context());
    int page = model.getPageByItem(37, 124, 1, true);
    assertTrue("compare into thaimc returned page 0 for a map gap", page > 0);
  }

  @Test
  public void mahaChula_getPagesByItem_missingItem_fallsBackToNearestLower() {
    ETThaiMahaChulaDataModel model = new ETThaiMahaChulaDataModel(context());
    // Jump-to-item by Siam indexing: i124 missing -> nearest lower i123.
    Integer[] pages = model.getPagesByItem(37, 124, true);
    assertTrue("jump-to-item returned no pages for a map gap", pages.length > 0);
    assertEquals(Integer.valueOf(69), pages[0]);
  }

  @Test
  public void mahaChula_getPagesByItem_presentItem_unchanged() {
    ETThaiMahaChulaDataModel model = new ETThaiMahaChulaDataModel(context());
    Integer[] pages = model.getPagesByItem(37, 125, true);
    assertTrue(pages.length > 0);
    assertEquals(Integer.valueOf(70), pages[0]);
  }

  // ── Maha Chula 2 (thaimc2) inherits the same map + algorithm ───────────

  @Test
  public void mahaChula2_missingItem_fallsBackToNearestLower() {
    ETThaiMahaChula2DataModel model = new ETThaiMahaChula2DataModel(context());
    // Same mc_map; inherited getPageByItem must apply the same fallback.
    assertEquals(69, model.getPageByItem(37, 124, 1, true));
    assertEquals(69, model.getPageByItem(37, 123, 1, true));
  }

  // ── Maha Supreme (thaims) ──────────────────────────────────────────────

  @Test
  public void supreme_presentItem_unchanged() {
    ETThaiSupremeDataModel model = new ETThaiSupremeDataModel(context());
    assertEquals(30, model.getPageByItem(37, 31, 1, true));
    assertEquals(32, model.getPageByItem(37, 34, 1, true));
  }

  @Test
  public void supreme_missingItem_fallsBackToNearestLower() {
    ETThaiSupremeDataModel model = new ETThaiSupremeDataModel(context());
    // i32 and i33 are absent; nearest lower present item is i31 (page 30).
    assertEquals(30, model.getPageByItem(37, 32, 1, true));
    assertEquals(30, model.getPageByItem(37, 33, 1, true));
  }

  @Test
  public void supreme_missingItem_doesNotReturnZero() {
    ETThaiSupremeDataModel model = new ETThaiSupremeDataModel(context());
    int page = model.getPageByItem(37, 33, 1, true);
    assertTrue("compare into thaims returned page 0 for a map gap", page > 0);
  }

  @Test
  public void supreme_getPagesByItem_missingItem_fallsBackToNearestLower() {
    ETThaiSupremeDataModel model = new ETThaiSupremeDataModel(context());
    Integer[] pages = model.getPagesByItem(37, 33, true);
    assertTrue("jump-to-item returned no pages for a map gap", pages.length > 0);
    assertEquals(Integer.valueOf(30), pages[0]);
  }
}
