// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: api/common/c_phonenumber.proto
// Protobuf Java Version: 4.26.1

package com.teneasyChat.api.common;

public final class CPhonenumber {
  private CPhonenumber() {}
  static {
    com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
      com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
      /* major= */ 4,
      /* minor= */ 26,
      /* patch= */ 1,
      /* suffix= */ "",
      CPhonenumber.class.getName());
  }
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface PhoneNumberOrBuilder extends
      // @@protoc_insertion_point(interface_extends:api.common.PhoneNumber)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <pre>
     * 默认中国区号 86
     * </pre>
     *
     * <code>int32 country_code = 1;</code>
     * @return The countryCode.
     */
    int getCountryCode();

    /**
     * <pre>
     * International Telecommunication Union (ITU) Recommendation E.164,
     * </pre>
     *
     * <code>int64 national_number = 2;</code>
     * @return The nationalNumber.
     */
    long getNationalNumber();

    /**
     * <pre>
     * 隐去手机号码部分数字后的表现形式, 如:
     * 133*****123
     * 通常用作前端表现, 或消费方不应知道完整手机号码的场景
     * </pre>
     *
     * <code>string masked_national_number = 3;</code>
     * @return The maskedNationalNumber.
     */
    java.lang.String getMaskedNationalNumber();
    /**
     * <pre>
     * 隐去手机号码部分数字后的表现形式, 如:
     * 133*****123
     * 通常用作前端表现, 或消费方不应知道完整手机号码的场景
     * </pre>
     *
     * <code>string masked_national_number = 3;</code>
     * @return The bytes for maskedNationalNumber.
     */
    com.google.protobuf.ByteString
        getMaskedNationalNumberBytes();
  }
  /**
   * Protobuf type {@code api.common.PhoneNumber}
   */
  public static final class PhoneNumber extends
      com.google.protobuf.GeneratedMessage implements
      // @@protoc_insertion_point(message_implements:api.common.PhoneNumber)
      PhoneNumberOrBuilder {
  private static final long serialVersionUID = 0L;
    static {
      com.google.protobuf.RuntimeVersion.validateProtobufGencodeVersion(
        com.google.protobuf.RuntimeVersion.RuntimeDomain.PUBLIC,
        /* major= */ 4,
        /* minor= */ 26,
        /* patch= */ 1,
        /* suffix= */ "",
        PhoneNumber.class.getName());
    }
    // Use PhoneNumber.newBuilder() to construct.
    private PhoneNumber(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
      super(builder);
    }
    private PhoneNumber() {
      maskedNationalNumber_ = "";
    }

    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.teneasyChat.api.common.CPhonenumber.internal_static_api_common_PhoneNumber_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.teneasyChat.api.common.CPhonenumber.internal_static_api_common_PhoneNumber_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.teneasyChat.api.common.CPhonenumber.PhoneNumber.class, com.teneasyChat.api.common.CPhonenumber.PhoneNumber.Builder.class);
    }

    public static final int COUNTRY_CODE_FIELD_NUMBER = 1;
    private int countryCode_ = 0;
    /**
     * <pre>
     * 默认中国区号 86
     * </pre>
     *
     * <code>int32 country_code = 1;</code>
     * @return The countryCode.
     */
    @java.lang.Override
    public int getCountryCode() {
      return countryCode_;
    }

    public static final int NATIONAL_NUMBER_FIELD_NUMBER = 2;
    private long nationalNumber_ = 0L;
    /**
     * <pre>
     * International Telecommunication Union (ITU) Recommendation E.164,
     * </pre>
     *
     * <code>int64 national_number = 2;</code>
     * @return The nationalNumber.
     */
    @java.lang.Override
    public long getNationalNumber() {
      return nationalNumber_;
    }

    public static final int MASKED_NATIONAL_NUMBER_FIELD_NUMBER = 3;
    @SuppressWarnings("serial")
    private volatile java.lang.Object maskedNationalNumber_ = "";
    /**
     * <pre>
     * 隐去手机号码部分数字后的表现形式, 如:
     * 133*****123
     * 通常用作前端表现, 或消费方不应知道完整手机号码的场景
     * </pre>
     *
     * <code>string masked_national_number = 3;</code>
     * @return The maskedNationalNumber.
     */
    @java.lang.Override
    public java.lang.String getMaskedNationalNumber() {
      java.lang.Object ref = maskedNationalNumber_;
      if (ref instanceof java.lang.String) {
        return (java.lang.String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        maskedNationalNumber_ = s;
        return s;
      }
    }
    /**
     * <pre>
     * 隐去手机号码部分数字后的表现形式, 如:
     * 133*****123
     * 通常用作前端表现, 或消费方不应知道完整手机号码的场景
     * </pre>
     *
     * <code>string masked_national_number = 3;</code>
     * @return The bytes for maskedNationalNumber.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString
        getMaskedNationalNumberBytes() {
      java.lang.Object ref = maskedNationalNumber_;
      if (ref instanceof java.lang.String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        maskedNationalNumber_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }

    private byte memoizedIsInitialized = -1;
    @java.lang.Override
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    @java.lang.Override
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (countryCode_ != 0) {
        output.writeInt32(1, countryCode_);
      }
      if (nationalNumber_ != 0L) {
        output.writeInt64(2, nationalNumber_);
      }
      if (!com.google.protobuf.GeneratedMessage.isStringEmpty(maskedNationalNumber_)) {
        com.google.protobuf.GeneratedMessage.writeString(output, 3, maskedNationalNumber_);
      }
      getUnknownFields().writeTo(output);
    }

    @java.lang.Override
    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (countryCode_ != 0) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(1, countryCode_);
      }
      if (nationalNumber_ != 0L) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt64Size(2, nationalNumber_);
      }
      if (!com.google.protobuf.GeneratedMessage.isStringEmpty(maskedNationalNumber_)) {
        size += com.google.protobuf.GeneratedMessage.computeStringSize(3, maskedNationalNumber_);
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof com.teneasyChat.api.common.CPhonenumber.PhoneNumber)) {
        return super.equals(obj);
      }
      com.teneasyChat.api.common.CPhonenumber.PhoneNumber other = (com.teneasyChat.api.common.CPhonenumber.PhoneNumber) obj;

      if (getCountryCode()
          != other.getCountryCode()) return false;
      if (getNationalNumber()
          != other.getNationalNumber()) return false;
      if (!getMaskedNationalNumber()
          .equals(other.getMaskedNationalNumber())) return false;
      if (!getUnknownFields().equals(other.getUnknownFields())) return false;
      return true;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      hash = (37 * hash) + COUNTRY_CODE_FIELD_NUMBER;
      hash = (53 * hash) + getCountryCode();
      hash = (37 * hash) + NATIONAL_NUMBER_FIELD_NUMBER;
      hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
          getNationalNumber());
      hash = (37 * hash) + MASKED_NATIONAL_NUMBER_FIELD_NUMBER;
      hash = (53 * hash) + getMaskedNationalNumber().hashCode();
      hash = (29 * hash) + getUnknownFields().hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static com.teneasyChat.api.common.CPhonenumber.PhoneNumber parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.teneasyChat.api.common.CPhonenumber.PhoneNumber parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.teneasyChat.api.common.CPhonenumber.PhoneNumber parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.teneasyChat.api.common.CPhonenumber.PhoneNumber parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.teneasyChat.api.common.CPhonenumber.PhoneNumber parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static com.teneasyChat.api.common.CPhonenumber.PhoneNumber parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static com.teneasyChat.api.common.CPhonenumber.PhoneNumber parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessage
          .parseWithIOException(PARSER, input);
    }
    public static com.teneasyChat.api.common.CPhonenumber.PhoneNumber parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessage
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public static com.teneasyChat.api.common.CPhonenumber.PhoneNumber parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessage
          .parseDelimitedWithIOException(PARSER, input);
    }

    public static com.teneasyChat.api.common.CPhonenumber.PhoneNumber parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessage
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static com.teneasyChat.api.common.CPhonenumber.PhoneNumber parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessage
          .parseWithIOException(PARSER, input);
    }
    public static com.teneasyChat.api.common.CPhonenumber.PhoneNumber parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessage
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    @java.lang.Override
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(com.teneasyChat.api.common.CPhonenumber.PhoneNumber prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    @java.lang.Override
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code api.common.PhoneNumber}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:api.common.PhoneNumber)
        com.teneasyChat.api.common.CPhonenumber.PhoneNumberOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return com.teneasyChat.api.common.CPhonenumber.internal_static_api_common_PhoneNumber_descriptor;
      }

      @java.lang.Override
      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return com.teneasyChat.api.common.CPhonenumber.internal_static_api_common_PhoneNumber_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                com.teneasyChat.api.common.CPhonenumber.PhoneNumber.class, com.teneasyChat.api.common.CPhonenumber.PhoneNumber.Builder.class);
      }

      // Construct using com.teneasyChat.api.common.CPhonenumber.PhoneNumber.newBuilder()
      private Builder() {

      }

      private Builder(
          com.google.protobuf.GeneratedMessage.BuilderParent parent) {
        super(parent);

      }
      @java.lang.Override
      public Builder clear() {
        super.clear();
        bitField0_ = 0;
        countryCode_ = 0;
        nationalNumber_ = 0L;
        maskedNationalNumber_ = "";
        return this;
      }

      @java.lang.Override
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return com.teneasyChat.api.common.CPhonenumber.internal_static_api_common_PhoneNumber_descriptor;
      }

      @java.lang.Override
      public com.teneasyChat.api.common.CPhonenumber.PhoneNumber getDefaultInstanceForType() {
        return com.teneasyChat.api.common.CPhonenumber.PhoneNumber.getDefaultInstance();
      }

      @java.lang.Override
      public com.teneasyChat.api.common.CPhonenumber.PhoneNumber build() {
        com.teneasyChat.api.common.CPhonenumber.PhoneNumber result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      @java.lang.Override
      public com.teneasyChat.api.common.CPhonenumber.PhoneNumber buildPartial() {
        com.teneasyChat.api.common.CPhonenumber.PhoneNumber result = new com.teneasyChat.api.common.CPhonenumber.PhoneNumber(this);
        if (bitField0_ != 0) { buildPartial0(result); }
        onBuilt();
        return result;
      }

      private void buildPartial0(com.teneasyChat.api.common.CPhonenumber.PhoneNumber result) {
        int from_bitField0_ = bitField0_;
        if (((from_bitField0_ & 0x00000001) != 0)) {
          result.countryCode_ = countryCode_;
        }
        if (((from_bitField0_ & 0x00000002) != 0)) {
          result.nationalNumber_ = nationalNumber_;
        }
        if (((from_bitField0_ & 0x00000004) != 0)) {
          result.maskedNationalNumber_ = maskedNationalNumber_;
        }
      }

      @java.lang.Override
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof com.teneasyChat.api.common.CPhonenumber.PhoneNumber) {
          return mergeFrom((com.teneasyChat.api.common.CPhonenumber.PhoneNumber)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(com.teneasyChat.api.common.CPhonenumber.PhoneNumber other) {
        if (other == com.teneasyChat.api.common.CPhonenumber.PhoneNumber.getDefaultInstance()) return this;
        if (other.getCountryCode() != 0) {
          setCountryCode(other.getCountryCode());
        }
        if (other.getNationalNumber() != 0L) {
          setNationalNumber(other.getNationalNumber());
        }
        if (!other.getMaskedNationalNumber().isEmpty()) {
          maskedNationalNumber_ = other.maskedNationalNumber_;
          bitField0_ |= 0x00000004;
          onChanged();
        }
        this.mergeUnknownFields(other.getUnknownFields());
        onChanged();
        return this;
      }

      @java.lang.Override
      public final boolean isInitialized() {
        return true;
      }

      @java.lang.Override
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        if (extensionRegistry == null) {
          throw new java.lang.NullPointerException();
        }
        try {
          boolean done = false;
          while (!done) {
            int tag = input.readTag();
            switch (tag) {
              case 0:
                done = true;
                break;
              case 8: {
                countryCode_ = input.readInt32();
                bitField0_ |= 0x00000001;
                break;
              } // case 8
              case 16: {
                nationalNumber_ = input.readInt64();
                bitField0_ |= 0x00000002;
                break;
              } // case 16
              case 26: {
                maskedNationalNumber_ = input.readStringRequireUtf8();
                bitField0_ |= 0x00000004;
                break;
              } // case 26
              default: {
                if (!super.parseUnknownField(input, extensionRegistry, tag)) {
                  done = true; // was an endgroup tag
                }
                break;
              } // default:
            } // switch (tag)
          } // while (!done)
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          throw e.unwrapIOException();
        } finally {
          onChanged();
        } // finally
        return this;
      }
      private int bitField0_;

      private int countryCode_ ;
      /**
       * <pre>
       * 默认中国区号 86
       * </pre>
       *
       * <code>int32 country_code = 1;</code>
       * @return The countryCode.
       */
      @java.lang.Override
      public int getCountryCode() {
        return countryCode_;
      }
      /**
       * <pre>
       * 默认中国区号 86
       * </pre>
       *
       * <code>int32 country_code = 1;</code>
       * @param value The countryCode to set.
       * @return This builder for chaining.
       */
      public Builder setCountryCode(int value) {

        countryCode_ = value;
        bitField0_ |= 0x00000001;
        onChanged();
        return this;
      }
      /**
       * <pre>
       * 默认中国区号 86
       * </pre>
       *
       * <code>int32 country_code = 1;</code>
       * @return This builder for chaining.
       */
      public Builder clearCountryCode() {
        bitField0_ = (bitField0_ & ~0x00000001);
        countryCode_ = 0;
        onChanged();
        return this;
      }

      private long nationalNumber_ ;
      /**
       * <pre>
       * International Telecommunication Union (ITU) Recommendation E.164,
       * </pre>
       *
       * <code>int64 national_number = 2;</code>
       * @return The nationalNumber.
       */
      @java.lang.Override
      public long getNationalNumber() {
        return nationalNumber_;
      }
      /**
       * <pre>
       * International Telecommunication Union (ITU) Recommendation E.164,
       * </pre>
       *
       * <code>int64 national_number = 2;</code>
       * @param value The nationalNumber to set.
       * @return This builder for chaining.
       */
      public Builder setNationalNumber(long value) {

        nationalNumber_ = value;
        bitField0_ |= 0x00000002;
        onChanged();
        return this;
      }
      /**
       * <pre>
       * International Telecommunication Union (ITU) Recommendation E.164,
       * </pre>
       *
       * <code>int64 national_number = 2;</code>
       * @return This builder for chaining.
       */
      public Builder clearNationalNumber() {
        bitField0_ = (bitField0_ & ~0x00000002);
        nationalNumber_ = 0L;
        onChanged();
        return this;
      }

      private java.lang.Object maskedNationalNumber_ = "";
      /**
       * <pre>
       * 隐去手机号码部分数字后的表现形式, 如:
       * 133*****123
       * 通常用作前端表现, 或消费方不应知道完整手机号码的场景
       * </pre>
       *
       * <code>string masked_national_number = 3;</code>
       * @return The maskedNationalNumber.
       */
      public java.lang.String getMaskedNationalNumber() {
        java.lang.Object ref = maskedNationalNumber_;
        if (!(ref instanceof java.lang.String)) {
          com.google.protobuf.ByteString bs =
              (com.google.protobuf.ByteString) ref;
          java.lang.String s = bs.toStringUtf8();
          maskedNationalNumber_ = s;
          return s;
        } else {
          return (java.lang.String) ref;
        }
      }
      /**
       * <pre>
       * 隐去手机号码部分数字后的表现形式, 如:
       * 133*****123
       * 通常用作前端表现, 或消费方不应知道完整手机号码的场景
       * </pre>
       *
       * <code>string masked_national_number = 3;</code>
       * @return The bytes for maskedNationalNumber.
       */
      public com.google.protobuf.ByteString
          getMaskedNationalNumberBytes() {
        java.lang.Object ref = maskedNationalNumber_;
        if (ref instanceof String) {
          com.google.protobuf.ByteString b = 
              com.google.protobuf.ByteString.copyFromUtf8(
                  (java.lang.String) ref);
          maskedNationalNumber_ = b;
          return b;
        } else {
          return (com.google.protobuf.ByteString) ref;
        }
      }
      /**
       * <pre>
       * 隐去手机号码部分数字后的表现形式, 如:
       * 133*****123
       * 通常用作前端表现, 或消费方不应知道完整手机号码的场景
       * </pre>
       *
       * <code>string masked_national_number = 3;</code>
       * @param value The maskedNationalNumber to set.
       * @return This builder for chaining.
       */
      public Builder setMaskedNationalNumber(
          java.lang.String value) {
        if (value == null) { throw new NullPointerException(); }
        maskedNationalNumber_ = value;
        bitField0_ |= 0x00000004;
        onChanged();
        return this;
      }
      /**
       * <pre>
       * 隐去手机号码部分数字后的表现形式, 如:
       * 133*****123
       * 通常用作前端表现, 或消费方不应知道完整手机号码的场景
       * </pre>
       *
       * <code>string masked_national_number = 3;</code>
       * @return This builder for chaining.
       */
      public Builder clearMaskedNationalNumber() {
        maskedNationalNumber_ = getDefaultInstance().getMaskedNationalNumber();
        bitField0_ = (bitField0_ & ~0x00000004);
        onChanged();
        return this;
      }
      /**
       * <pre>
       * 隐去手机号码部分数字后的表现形式, 如:
       * 133*****123
       * 通常用作前端表现, 或消费方不应知道完整手机号码的场景
       * </pre>
       *
       * <code>string masked_national_number = 3;</code>
       * @param value The bytes for maskedNationalNumber to set.
       * @return This builder for chaining.
       */
      public Builder setMaskedNationalNumberBytes(
          com.google.protobuf.ByteString value) {
        if (value == null) { throw new NullPointerException(); }
        checkByteStringIsUtf8(value);
        maskedNationalNumber_ = value;
        bitField0_ |= 0x00000004;
        onChanged();
        return this;
      }

      // @@protoc_insertion_point(builder_scope:api.common.PhoneNumber)
    }

    // @@protoc_insertion_point(class_scope:api.common.PhoneNumber)
    private static final com.teneasyChat.api.common.CPhonenumber.PhoneNumber DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new com.teneasyChat.api.common.CPhonenumber.PhoneNumber();
    }

    public static com.teneasyChat.api.common.CPhonenumber.PhoneNumber getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<PhoneNumber>
        PARSER = new com.google.protobuf.AbstractParser<PhoneNumber>() {
      @java.lang.Override
      public PhoneNumber parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        Builder builder = newBuilder();
        try {
          builder.mergeFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          throw e.setUnfinishedMessage(builder.buildPartial());
        } catch (com.google.protobuf.UninitializedMessageException e) {
          throw e.asInvalidProtocolBufferException().setUnfinishedMessage(builder.buildPartial());
        } catch (java.io.IOException e) {
          throw new com.google.protobuf.InvalidProtocolBufferException(e)
              .setUnfinishedMessage(builder.buildPartial());
        }
        return builder.buildPartial();
      }
    };

    public static com.google.protobuf.Parser<PhoneNumber> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<PhoneNumber> getParserForType() {
      return PARSER;
    }

    @java.lang.Override
    public com.teneasyChat.api.common.CPhonenumber.PhoneNumber getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_api_common_PhoneNumber_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_api_common_PhoneNumber_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\036api/common/c_phonenumber.proto\022\napi.co" +
      "mmon\"\\\n\013PhoneNumber\022\024\n\014country_code\030\001 \001(" +
      "\005\022\027\n\017national_number\030\002 \001(\003\022\036\n\026masked_nat" +
      "ional_number\030\003 \001(\tB<\n\032com.teneasyChat.ap" +
      "i.commonZ\025wcs/api/common;common\272\002\006Common" +
      "b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        });
    internal_static_api_common_PhoneNumber_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_api_common_PhoneNumber_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessage.FieldAccessorTable(
        internal_static_api_common_PhoneNumber_descriptor,
        new java.lang.String[] { "CountryCode", "NationalNumber", "MaskedNationalNumber", });
    descriptor.resolveAllFeaturesImmutable();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
