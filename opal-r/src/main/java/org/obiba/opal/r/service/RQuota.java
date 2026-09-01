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

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.obiba.opal.core.domain.AbstractTimestamped;
import org.obiba.opal.core.domain.converter.EnumNameConverter;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * An allowance of R execution time, for a subject and an execution context.
 * <p>
 * A quota says nothing by itself: what is consumed against it is the R execution time already recorded in
 * {@link RSessionActivity}, summed over the rolling window of its {@link Period}. See {@link RQuotaService} for how one
 * is picked for a given user.
 */
@Entity
@Table(name = "r_quotas",
    uniqueConstraints = @UniqueConstraint(name = "uk_r_quotas", columnNames = {"context", "subject_type", "principal"}))
public class RQuota extends AbstractTimestamped {

  /**
   * The subject of the system-wide default, which has none.
   * <p>
   * It is the empty string rather than NULL because the unique constraint has to mean the same thing on both servers:
   * PostgreSQL counts NULLs as distinct, so a nullable column would let two system defaults coexist there and not on
   * H2.
   */
  public static final String SYSTEM_PRINCIPAL = "";

  public enum SubjectType {
    SYSTEM, GROUP, USER
  }

  /**
   * The window usage is summed over. It rolls: there is no reset instant, capacity returns as old activity ages out.
   */
  public enum Period {

    DAILY(TimeUnit.DAYS.toMillis(1)),

    WEEKLY(TimeUnit.DAYS.toMillis(7));

    private final long durationMillis;

    Period(long durationMillis) {
      this.durationMillis = durationMillis;
    }

    public long getDurationMillis() {
      return durationMillis;
    }

    public Date getWindowStart(Date now) {
      return new Date(now.getTime() - durationMillis);
    }
  }

  @Converter
  public static class SubjectTypeConverter extends EnumNameConverter<SubjectType> {
    public SubjectTypeConverter() {
      super(SubjectType.class);
    }
  }

  @Converter
  public static class PeriodConverter extends EnumNameConverter<Period> {
    public PeriodConverter() {
      super(Period.class);
    }
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @NotBlank
  @Column(nullable = false)
  private String context;

  @NotNull
  @Column(name = "subject_type", nullable = false)
  @Convert(converter = SubjectTypeConverter.class)
  private SubjectType subjectType = SubjectType.SYSTEM;

  /**
   * The user name or the group name, {@link #SYSTEM_PRINCIPAL} for the system default.
   */
  @NotNull
  @Column(nullable = false)
  private String principal = SYSTEM_PRINCIPAL;

  @NotNull
  @Column(nullable = false)
  @Convert(converter = PeriodConverter.class)
  private Period period = Period.WEEKLY;

  /**
   * Zero is a meaningful value: it forbids the context altogether for this subject. "No quota" is the absence of a
   * row, not a limit of zero.
   */
  @Column(name = "execution_time_limit_millis", nullable = false)
  private long executionTimeLimitMillis = 0;

  @Column(nullable = false)
  private boolean enabled = true;

  public RQuota() {
  }

  public Long getId() {
    return id;
  }

  public String getContext() {
    return context;
  }

  public void setContext(String context) {
    this.context = context;
  }

  public SubjectType getSubjectType() {
    return subjectType;
  }

  public void setSubjectType(SubjectType subjectType) {
    this.subjectType = subjectType;
  }

  public String getPrincipal() {
    return principal;
  }

  public void setPrincipal(String principal) {
    this.principal = principal == null ? SYSTEM_PRINCIPAL : principal;
  }

  public Period getPeriod() {
    return period;
  }

  public void setPeriod(Period period) {
    this.period = period;
  }

  public long getExecutionTimeLimitMillis() {
    return executionTimeLimitMillis;
  }

  public void setExecutionTimeLimitMillis(long executionTimeLimitMillis) {
    this.executionTimeLimitMillis = executionTimeLimitMillis;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public Date getWindowStart(Date now) {
    return period.getWindowStart(now);
  }
}
