/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

toResource = function(name, params, credentials) {
  return {
    name: name,
    url: "file://" + params.path,
    format: params.format,
    identity: credentials.username,
    secret: credentials.password
  }
}