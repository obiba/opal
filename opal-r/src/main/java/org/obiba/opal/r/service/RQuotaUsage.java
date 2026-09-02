/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.service;

import jakarta.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * What a user has consumed, for one {@link RQuota.Metric}, against the quota that applies to them if one does.
 */
public class RQuotaUsage {

  private static final String CREDIT_DATE_FORMAT = "yyyy-MM-dd HH:mm";

  private final String context;

  private final String user;

  private final RQuota.Metric metric;

  /**
   * The quota that applies, or null when none does: no quota means unlimited, never a limit of zero.
   */
  private final RQuota quota;

  private final long usedMillis;

  private final Date windowStart;

  /**
   * When the oldest activity still counted leaves the window, so that some capacity returns. Only computed when the
   * quota is exceeded, since that is the only time anyone is waiting for it.
   */
  private final Date nextCreditDate;

  /**
   * How many sessions the user has open in the context. It is what makes a session time refusal actionable: those
   * sessions keep spending the allowance, and no amount of waiting takes their contribution back out of the window.
   */
  private final int openSessionsCount;

  private RQuotaUsage(String context, String user, RQuota.Metric metric, @Nullable RQuota quota, long usedMillis,
                      @Nullable Date windowStart, @Nullable Date nextCreditDate, int openSessionsCount) {
    this.context = context;
    this.user = user;
    this.metric = metric;
    this.quota = quota;
    this.usedMillis = usedMillis;
    this.windowStart = windowStart;
    this.nextCreditDate = nextCreditDate;
    this.openSessionsCount = openSessionsCount;
  }

  /**
   * No quota applies to this metric: nothing is measured, because there is nothing to measure it against.
   */
  public static RQuotaUsage unlimited(String context, String user, RQuota.Metric metric) {
    return new RQuotaUsage(context, user, metric, null, 0, null, null, 0);
  }

  public static RQuotaUsage of(String context, String user, RQuota quota, long usedMillis, Date windowStart,
                               @Nullable Date nextCreditDate, int openSessionsCount) {
    return new RQuotaUsage(context, user, quota.getMetric(), quota, usedMillis, windowStart, nextCreditDate,
        openSessionsCount);
  }

  public String getContext() {
    return context;
  }

  public String getUser() {
    return user;
  }

  public RQuota.Metric getMetric() {
    return metric;
  }

  @Nullable
  public RQuota getQuota() {
    return quota;
  }

  public boolean hasQuota() {
    return quota != null;
  }

  public long getUsedMillis() {
    return usedMillis;
  }

  @Nullable
  public Date getWindowStart() {
    return windowStart;
  }

  @Nullable
  public Date getNextCreditDate() {
    return nextCreditDate;
  }

  public int getOpenSessionsCount() {
    return openSessionsCount;
  }

  /**
   * Spending exactly the allowance exhausts it, which is what makes a limit of zero forbid the context outright.
   */
  public boolean isExceeded() {
    return quota != null && usedMillis >= quota.getLimitMillis();
  }

  /**
   * Names the metric, because a user who frees up one of them and is still refused has learnt nothing from a message
   * that only named the other. On session time the useful advice is not to wait but to close what is open, so the
   * message says that instead of a credit date it cannot honour.
   */
  public String asMessage() {
    if (!isExceeded()) return "";
    StringBuilder message = new StringBuilder(String.format("%s quota exceeded: %d of %d minutes of %s used in the last %s.",
        context,
        TimeUnit.MILLISECONDS.toMinutes(usedMillis),
        TimeUnit.MILLISECONDS.toMinutes(quota.getLimitMillis()),
        asMetricLabel(metric),
        asWindowLabel(quota.getPeriod())));
    if (RQuota.Metric.SESSION_TIME.equals(metric) && openSessionsCount > 0)
      message.append(String.format(" You have %d %s session%s open; they keep consuming your allowance until you close them.",
          openSessionsCount, context, openSessionsCount > 1 ? "s" : ""));
    else if (nextCreditDate != null)
      message.append(String.format(" Some capacity returns on %s.",
          new SimpleDateFormat(CREDIT_DATE_FORMAT).format(nextCreditDate)));
    return message.toString();
  }

  private static String asMetricLabel(RQuota.Metric metric) {
    return RQuota.Metric.SESSION_TIME.equals(metric) ? "session time" : "execution time";
  }

  private static String asWindowLabel(RQuota.Period period) {
    long hours = TimeUnit.MILLISECONDS.toHours(period.getDurationMillis());
    return hours < 48 ? hours + " hours" : TimeUnit.MILLISECONDS.toDays(period.getDurationMillis()) + " days";
  }
}
