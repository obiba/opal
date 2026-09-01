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
 * What a user has consumed against the quota that applies to them, if one does.
 */
public class RQuotaUsage {

  private static final String CREDIT_DATE_FORMAT = "yyyy-MM-dd HH:mm";

  private final String context;

  private final String user;

  /**
   * The quota that applies, or null when none does: no quota means unlimited, never a limit of zero.
   */
  private final RQuota quota;

  private final long usedExecutionTimeMillis;

  private final Date windowStart;

  /**
   * When the oldest activity still counted leaves the window, so that some capacity returns. Only computed when the
   * quota is exceeded, since that is the only time anyone is waiting for it.
   */
  private final Date nextCreditDate;

  private RQuotaUsage(String context, String user, @Nullable RQuota quota, long usedExecutionTimeMillis,
                      @Nullable Date windowStart, @Nullable Date nextCreditDate) {
    this.context = context;
    this.user = user;
    this.quota = quota;
    this.usedExecutionTimeMillis = usedExecutionTimeMillis;
    this.windowStart = windowStart;
    this.nextCreditDate = nextCreditDate;
  }

  /**
   * No quota applies: nothing is measured, because there is nothing to measure it against.
   */
  public static RQuotaUsage unlimited(String context, String user) {
    return new RQuotaUsage(context, user, null, 0, null, null);
  }

  public static RQuotaUsage of(String context, String user, RQuota quota, long usedExecutionTimeMillis,
                               Date windowStart, @Nullable Date nextCreditDate) {
    return new RQuotaUsage(context, user, quota, usedExecutionTimeMillis, windowStart, nextCreditDate);
  }

  public String getContext() {
    return context;
  }

  public String getUser() {
    return user;
  }

  @Nullable
  public RQuota getQuota() {
    return quota;
  }

  public boolean hasQuota() {
    return quota != null;
  }

  public long getUsedExecutionTimeMillis() {
    return usedExecutionTimeMillis;
  }

  @Nullable
  public Date getWindowStart() {
    return windowStart;
  }

  @Nullable
  public Date getNextCreditDate() {
    return nextCreditDate;
  }

  /**
   * Spending exactly the allowance exhausts it, which is what makes a limit of zero forbid the context outright.
   */
  public boolean isExceeded() {
    return quota != null && usedExecutionTimeMillis >= quota.getExecutionTimeLimitMillis();
  }

  public String asMessage() {
    if (!isExceeded()) return "";
    StringBuilder message = new StringBuilder(String.format("%s quota exceeded: %d of %d minutes used in the last %s.",
        context,
        TimeUnit.MILLISECONDS.toMinutes(usedExecutionTimeMillis),
        TimeUnit.MILLISECONDS.toMinutes(quota.getExecutionTimeLimitMillis()),
        asWindowLabel(quota.getPeriod())));
    if (nextCreditDate != null) message.append(String.format(" Some capacity returns on %s.",
        new SimpleDateFormat(CREDIT_DATE_FORMAT).format(nextCreditDate)));
    return message.toString();
  }

  private static String asWindowLabel(RQuota.Period period) {
    long hours = TimeUnit.MILLISECONDS.toHours(period.getDurationMillis());
    return hours < 48 ? hours + " hours" : TimeUnit.MILLISECONDS.toDays(period.getDurationMillis()) + " days";
  }
}
