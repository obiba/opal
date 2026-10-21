/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.shell.repository;

import org.obiba.opal.shell.domain.TaskState;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The task history. It is read whole once, at startup, and written as tasks are submitted, finish and are deleted.
 */
public interface TaskStateRepository extends JpaRepository<TaskState, Integer> {
}
