package org.obiba.opal.core.service;

import com.google.cloud.tools.jib.api.ImageReference;
import com.google.cloud.tools.jib.api.InvalidImageReferenceException;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class PodServiceImplTest {

  @Test
  public void imageRefWithTagTest() {
    try {
      ImageReference imageReference = ImageReference.parse("obiba/rock:2.0.0");
      assertThat(imageReference.getRepository()).isEqualTo("obiba/rock");
      assertThat(imageReference.getTag().orElse(null)).isEqualTo("2.0.0");
      assertThat(imageReference.getDigest().orElse(null)).isNull();
      assertThat(imageReference.getQualifier()).isEqualTo("2.0.0");
    } catch (InvalidImageReferenceException e) {
      assertThat(true).isFalse();
    }
  }

  @Test
  public void imageRefNoTagTest() {
    try {
      ImageReference imageReference = ImageReference.parse("obiba/rock");
      assertThat(imageReference.getRepository()).isEqualTo("obiba/rock");
      assertThat(imageReference.getTag().orElse(null)).isEqualTo("latest");
      assertThat(imageReference.getDigest().orElse(null)).isNull();
      assertThat(imageReference.getQualifier()).isEqualTo("latest");
    } catch (InvalidImageReferenceException e) {
      assertThat(true).isFalse();
    }
  }

  // ghcr.io/epfl-enac/ethz-alice/arema/backend

  @Test
  public void imageRefGHRegistryWithDigestTest() {
    try {
      ImageReference imageReference = ImageReference.parse("ghcr.io/epfl-enac/ethz-alice/arema/backend@sha256:439c4ebbe057cac0ed027fdf4607829521782dc3ed969cf33dc396f3f8e7b244");
      assertThat(imageReference.getRegistry()).isEqualTo("ghcr.io");
      assertThat(imageReference.getRepository()).isEqualTo("epfl-enac/ethz-alice/arema/backend");
      assertThat(imageReference.getTag().orElse(null)).isNull();
      assertThat(imageReference.getDigest().orElse(null)).isEqualTo("sha256:439c4ebbe057cac0ed027fdf4607829521782dc3ed969cf33dc396f3f8e7b244");
      assertThat(imageReference.getQualifier()).isEqualTo("sha256:439c4ebbe057cac0ed027fdf4607829521782dc3ed969cf33dc396f3f8e7b244");
    } catch (InvalidImageReferenceException e) {
      assertThat(true).isFalse();
    }
  }

  @Test
  public void imageRefComparisonTest() {
    try {
      ImageReference allowedReference = ImageReference.parse("ghcr.io/epfl-enac/ethz-alice/arema/backend");
      ImageReference imageReference = ImageReference.parse("ghcr.io/epfl-enac/ethz-alice/arema/backend@sha256:439c4ebbe057cac0ed027fdf4607829521782dc3ed969cf33dc396f3f8e7b244");
      assertThat(imageReference.getRegistry()).isEqualTo(allowedReference.getRegistry());
      assertThat(imageReference.getRepository()).isEqualTo(allowedReference.getRepository());
    } catch (InvalidImageReferenceException e) {
      assertThat(true).isFalse();
    }
  }

}
