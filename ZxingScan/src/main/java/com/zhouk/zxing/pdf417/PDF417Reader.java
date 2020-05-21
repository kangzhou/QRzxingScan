/*
 * Copyright 2009 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zhouk.zxing.pdf417;

import com.zhouk.zxing.BarcodeFormat;
import com.zhouk.zxing.BinaryBitmap;
import com.zhouk.zxing.ChecksumException;
import com.zhouk.zxing.DecodeHintType;
import com.zhouk.zxing.FormatException;
import com.zhouk.zxing.NotFoundException;
import com.zhouk.zxing.Reader;
import com.zhouk.zxing.Result;
import com.zhouk.zxing.ResultMetadataType;
import com.zhouk.zxing.ResultPoint;
import com.zhouk.zxing.common.DecoderResult;
import com.zhouk.zxing.multi.MultipleBarcodeReader;
import com.zhouk.zxing.pdf417.PDF417Common;
import com.zhouk.zxing.pdf417.PDF417ResultMetadata;
import com.zhouk.zxing.pdf417.decoder.PDF417ScanningDecoder;
import com.zhouk.zxing.pdf417.detector.Detector;
import com.zhouk.zxing.pdf417.detector.PDF417DetectorResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This implementation can detect and decode PDF417 codes in an image.
 *
 * @author Guenther Grau
 */
public final class PDF417Reader implements Reader, MultipleBarcodeReader {

  private static final Result[] EMPTY_RESULT_ARRAY = new Result[0];

  /**
   * Locates and decodes a PDF417 code in an image.
   *
   * @return a String representing the content encoded by the PDF417 code
   * @throws NotFoundException if a PDF417 code cannot be found,
   * @throws FormatException if a PDF417 cannot be decoded
   */
  @Override
  public Result decode(BinaryBitmap image) throws NotFoundException, FormatException, ChecksumException {
    return decode(image, null);
  }

  @Override
  public Result decode(BinaryBitmap image, Map<DecodeHintType,?> hints) throws NotFoundException, FormatException,
      ChecksumException {
    Result[] result = decode(image, hints, false);
    if (result.length == 0 || result[0] == null) {
      throw NotFoundException.getNotFoundInstance();
    }
    return result[0];
  }

  @Override
  public Result[] decodeMultiple(BinaryBitmap image) throws NotFoundException {
    return decodeMultiple(image, null);
  }

  @Override
  public Result[] decodeMultiple(BinaryBitmap image, Map<DecodeHintType,?> hints) throws NotFoundException {
    try {
      return decode(image, hints, true);
    } catch (FormatException | ChecksumException ignored) {
      throw NotFoundException.getNotFoundInstance();
    }
  }

  private static Result[] decode(BinaryBitmap image, Map<DecodeHintType, ?> hints, boolean multiple) 
      throws NotFoundException, FormatException, ChecksumException {
    List<Result> results = new ArrayList<>();
    PDF417DetectorResult detectorResult = Detector.detect(image, hints, multiple);
    for (ResultPoint[] points : detectorResult.getPoints()) {
      DecoderResult decoderResult = PDF417ScanningDecoder.decode(detectorResult.getBits(), points[4], points[5],
          points[6], points[7], getMinCodewordWidth(points), getMaxCodewordWidth(points));
      Result result = new Result(decoderResult.getText(), decoderResult.getRawBytes(), points, BarcodeFormat.PDF_417);
      result.putMetadata(ResultMetadataType.ERROR_CORRECTION_LEVEL, decoderResult.getECLevel());
      PDF417ResultMetadata pdf417ResultMetadata = (PDF417ResultMetadata) decoderResult.getOther();
      if (pdf417ResultMetadata != null) {
        result.putMetadata(ResultMetadataType.PDF417_EXTRA_METADATA, pdf417ResultMetadata);
      }
      results.add(result);
    }
    return results.toArray(EMPTY_RESULT_ARRAY);
  }

  private static int getMaxWidth(ResultPoint p1, ResultPoint p2) {
    if (p1 == null || p2 == null) {
      return 0;
    }
    return (int) Math.abs(p1.getX() - p2.getX());
  }

  private static int getMinWidth(ResultPoint p1, ResultPoint p2) {
    if (p1 == null || p2 == null) {
      return Integer.MAX_VALUE;
    }
    return (int) Math.abs(p1.getX() - p2.getX());
  }

  private static int getMaxCodewordWidth(ResultPoint[] p) {
    return Math.max(
        Math.max(getMaxWidth(p[0], p[4]), getMaxWidth(p[6], p[2]) * com.zhouk.zxing.pdf417.PDF417Common.MODULES_IN_CODEWORD /
            com.zhouk.zxing.pdf417.PDF417Common.MODULES_IN_STOP_PATTERN),
        Math.max(getMaxWidth(p[1], p[5]), getMaxWidth(p[7], p[3]) * com.zhouk.zxing.pdf417.PDF417Common.MODULES_IN_CODEWORD /
            com.zhouk.zxing.pdf417.PDF417Common.MODULES_IN_STOP_PATTERN));
  }

  private static int getMinCodewordWidth(ResultPoint[] p) {
    return Math.min(
        Math.min(getMinWidth(p[0], p[4]), getMinWidth(p[6], p[2]) * com.zhouk.zxing.pdf417.PDF417Common.MODULES_IN_CODEWORD /
            com.zhouk.zxing.pdf417.PDF417Common.MODULES_IN_STOP_PATTERN),
        Math.min(getMinWidth(p[1], p[5]), getMinWidth(p[7], p[3]) * com.zhouk.zxing.pdf417.PDF417Common.MODULES_IN_CODEWORD /
            PDF417Common.MODULES_IN_STOP_PATTERN));
  }

  @Override
  public void reset() {
    // nothing needs to be reset
  }

}
