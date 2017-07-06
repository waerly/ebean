package org.tests.cache;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.bean.BeanCollection;
import io.ebean.cache.ServerCache;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.tests.model.cache.EColAB;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestQueryCache extends BaseTestCase {

  @Test
  public void clashHashCode() {

    new EColAB("01", "20").save();
    new EColAB("02", "10").save();

    List<EColAB> list1 =
      Ebean.getServer(null)
        .find(EColAB.class)
        .setUseQueryCache(true)
        .where()
        .eq("columnA", "01")
        .eq("columnB", "20")
        .findList();

    List<EColAB> list2 =
      Ebean.getServer(null)
        .find(EColAB.class)
        .setUseQueryCache(true)
        .where()
        .eq("columnA", "02")
        .eq("columnB", "10")
        .findList();

    assertThat(list1.get(0).getColumnA()).isEqualTo("01");
    assertThat(list1.get(0).getColumnB()).isEqualTo("20");

    assertThat(list2.get(0).getColumnA()).isEqualTo("02");
    assertThat(list2.get(0).getColumnB()).isEqualTo("10");
  }

  @Test
  public void findSingleAttribute() {

    new EColAB("03", "SingleAttribute").save();
    new EColAB("03", "SingleAttribute").save();

    List<String> colA_first = Ebean.getServer(null)
      .find(EColAB.class)
      .setUseQueryCache(true)
      .setDistinct(true)
      .select("columnA")
      .where()
      .eq("columnB", "SingleAttribute")
      .findSingleAttributeList();

    List<String> colA_Second = Ebean.getServer(null)
      .find(EColAB.class)
      .setUseQueryCache(true)
      .setDistinct(true)
      .select("columnA")
      .where()
      .eq("columnB", "SingleAttribute")
      .findSingleAttributeList();

    assertThat(colA_Second).isSameAs(colA_first);

    List<String> colA_NotDistinct = Ebean.getServer(null)
      .find(EColAB.class)
      .setUseQueryCache(true)
      .select("columnA")
      .where()
      .eq("columnB", "SingleAttribute")
      .findSingleAttributeList();

    assertThat(colA_Second).isNotSameAs(colA_NotDistinct);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void test() {

    ResetBasicData.reset();

    ServerCache customerCache = Ebean.getServerCacheManager().getQueryCache(Customer.class);
    customerCache.clear();

    List<Customer> list = Ebean.find(Customer.class).setUseQueryCache(true).setReadOnly(true).where()
      .ilike("name", "Rob").findList();

    BeanCollection<Customer> bc = (BeanCollection<Customer>) list;
    Assert.assertFalse(bc.isReadOnly());
    Assert.assertFalse(bc.isEmpty());
    Assert.assertTrue(!list.isEmpty());
    Assert.assertTrue(Ebean.getBeanState(list.get(0)).isReadOnly());

    List<Customer> list2 = Ebean.find(Customer.class).setUseQueryCache(true).setReadOnly(true).where()
      .ilike("name", "Rob").findList();

    List<Customer> list2B = Ebean.find(Customer.class).setUseQueryCache(true)
      // .setReadOnly(true)
      .where().ilike("name", "Rob").findList();

    Assert.assertSame(list, list2);

    // readOnly defaults to true for query cache
    Assert.assertSame(list, list2B);


    // TODO: At this stage setReadOnly(false) does not
    // create a shallow copy of the List/Set/Map

//    List<Customer> list3 = Ebean.find(Customer.class).setUseQueryCache(true).setReadOnly(false).where()
//        .ilike("name", "Rob").findList();
//
//    Assert.assertNotSame(list, list3);
//    BeanCollection<Customer> bc3 = (BeanCollection<Customer>) list3;
//    Assert.assertFalse(bc3.isReadOnly());
//    Assert.assertFalse(bc3.isEmpty());
//    Assert.assertTrue(list3.size() > 0);
//    Assert.assertFalse(Ebean.getBeanState(list3.get(0)).isReadOnly());

  }

}
