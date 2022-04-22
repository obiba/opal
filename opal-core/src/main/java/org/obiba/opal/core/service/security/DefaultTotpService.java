/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.security;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import dev.samstevens.totp.util.Utils;
import org.obiba.opal.core.service.OpalGeneralConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class DefaultTotpService implements TotpService {

  private static final int DEFAULT_PERIOD = 30;

  private static final int DEFAULT_DIGITS = 6;

  private static final int DEFAULT_SECRET_LENGTH = 64;

  private static final String DEFAULT_HASH_ALGORITHM = "SHA1";

  private final OpalGeneralConfigService serverService;

  @Autowired
  private CryptoService cryptoService;

  @Autowired
  public DefaultTotpService(OpalGeneralConfigService serverService) {
    this.serverService = serverService;
  }

  @Override
  public String generateSecret() {
    // 32 chars long secret
    SecretGenerator generator = new DefaultSecretGenerator(DEFAULT_SECRET_LENGTH);
    return cryptoService.encrypt(generator.generate());
  }

  @Override
  public String getQrImageDataUri(String label, String secret) {
    String serverName = serverService.getConfig().getName();
    QrData data = new QrData.Builder()
        .label(label)
        .secret(cryptoService.decrypt(secret))
        .issuer(serverName)
        .algorithm(HashingAlgorithm.valueOf(DEFAULT_HASH_ALGORITHM))
        .digits(DEFAULT_DIGITS)
        .period(DEFAULT_PERIOD)
        .build();

    String dataUri;
    try {
      QrGenerator generator = new ZxingPngQrGenerator();
      byte[] imageData = generator.generate(data);
      String mimeType = generator.getImageMimeType();
      dataUri = Utils.getDataUriForImage(imageData, mimeType);
    } catch (QrGenerationException e) {
      throw new RuntimeException(e);
    }

    return dataUri;
  }

  @Override
  public boolean validateCode(String code, String secret) {
    TimeProvider timeProvider = new SystemTimeProvider();
    CodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.valueOf(DEFAULT_HASH_ALGORITHM), DEFAULT_DIGITS);
    CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
    return verifier.isValidCode(cryptoService.decrypt(secret), code);
  }
}
