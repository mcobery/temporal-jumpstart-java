/*
 * MIT License
 *
 * Copyright (c) 2024 temporal.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.temporal.jumpstart.starters.messages;

public class OnboardingsGet {

  private String id;
  private String executionStatus;
  private OnboardEntityRequest oer;

  public OnboardingsGet(String id, String executionStatus, OnboardEntityRequest oer) {
    this.id = id;
    this.executionStatus = executionStatus;
    this.oer = oer;
  }

  public OnboardingsGet() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getExecutionStatus() {
    return executionStatus;
  }

  public void setExecutionStatus(String executionStatus) {
    this.executionStatus = executionStatus;
  }

  public OnboardEntityRequest getOnboardingEntityRequest() {
    return oer;
  }

  public void setValue(OnboardEntityRequest oer) {
    this.oer = oer;
  }
}
