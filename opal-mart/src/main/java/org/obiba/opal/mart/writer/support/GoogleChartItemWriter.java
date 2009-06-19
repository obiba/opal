package org.obiba.opal.mart.writer.support;

import java.util.List;

import org.obiba.opal.mart.aggregate.AggregateQueryResult;
import org.springframework.batch.item.ItemWriter;

public class GoogleChartItemWriter implements ItemWriter<AggregateQueryResult> {

  public void write(List<? extends AggregateQueryResult> items) throws Exception {
    AggregateQueryResult aqr = items.get(0);

    StringBuilder sb = new StringBuilder("http://chart.apis.google.com/chart?chs=400x200&chds=0,$max&chxt=y&chxr=0,0,$max&chbh=a&cht=bvs&chl=");

    StringBuilder values = new StringBuilder();
    Integer maxValue = 0;

    for (Object key : aqr.keys()) {
      Integer value = aqr.getValue(key);
      if (values.length() > 0) {
        values.append(',');
      }
      values.append(value);
      if (value > maxValue) {
        maxValue = value;
      }
      sb.append(key).append('|');
    }
    sb.deleteCharAt(sb.length() - 1);
    sb.append("&chd=t:").append(values);
    sb.append("&chtt=").append("Category Frequency of ").append(aqr.getAggregateKey());

    System.out.println(sb.toString().replaceAll("\\$max", maxValue.toString()));
  }

}
