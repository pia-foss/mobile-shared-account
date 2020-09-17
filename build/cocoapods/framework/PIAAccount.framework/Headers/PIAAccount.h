#import <Foundation/NSArray.h>
#import <Foundation/NSDictionary.h>
#import <Foundation/NSError.h>
#import <Foundation/NSObject.h>
#import <Foundation/NSSet.h>
#import <Foundation/NSString.h>
#import <Foundation/NSValue.h>

@class PIAAAccountInformation, PIAAAccountRequestError, PIAAClientStatusInformation, PIAAInvitesDetailsInformation, PIAARedeemInformation, PIAAAccountBuilder, PIAAPlatform, PIAAAndroidSignupInformation, PIAASignUpInformation, PIAAAndroidSubscriptionsInformation, PIAAIOSPaymentInformation, PIAAIOSSignupInformation, PIAAIOSSubscriptionInformation, PIAAKotlinEnum, PIAAAndroidSignupInformationReceipt, PIAAAndroidSubscriptionsInformationAvailableProduct, PIAAIOSSubscriptionInformationAvailableProduct, PIAAIOSSubscriptionInformationReceipt, PIAAInvitesDetailsInformationInvite, PIAAKotlinArray, PIAAKotlinx_serialization_runtimeSerialKind, PIAAKotlinNothing, PIAAKotlinx_serialization_runtimeUpdateMode;

@protocol PIAAAccountAPI, PIAAKotlinComparable, PIAAKotlinx_serialization_runtimeKSerializer, PIAAKotlinx_serialization_runtimeEncoder, PIAAKotlinx_serialization_runtimeSerialDescriptor, PIAAKotlinx_serialization_runtimeSerializationStrategy, PIAAKotlinx_serialization_runtimeDecoder, PIAAKotlinx_serialization_runtimeDeserializationStrategy, PIAAKotlinx_serialization_runtimeCompositeEncoder, PIAAKotlinx_serialization_runtimeSerialModule, PIAAKotlinAnnotation, PIAAKotlinx_serialization_runtimeCompositeDecoder, PIAAKotlinIterator, PIAAKotlinx_serialization_runtimeSerialModuleCollector, PIAAKotlinKClass, PIAAKotlinKDeclarationContainer, PIAAKotlinKAnnotatedElement, PIAAKotlinKClassifier;

NS_ASSUME_NONNULL_BEGIN
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunknown-warning-option"
#pragma clang diagnostic ignored "-Wnullability"

__attribute__((swift_name("KotlinBase")))
@interface PIAABase : NSObject
- (instancetype)init __attribute__((unavailable));
+ (instancetype)new __attribute__((unavailable));
+ (void)initialize __attribute__((objc_requires_super));
@end;

@interface PIAABase (PIAABaseCopying) <NSCopying>
@end;

__attribute__((swift_name("KotlinMutableSet")))
@interface PIAAMutableSet<ObjectType> : NSMutableSet<ObjectType>
@end;

__attribute__((swift_name("KotlinMutableDictionary")))
@interface PIAAMutableDictionary<KeyType, ObjectType> : NSMutableDictionary<KeyType, ObjectType>
@end;

@interface NSError (NSErrorPIAAKotlinException)
@property (readonly) id _Nullable kotlinException;
@end;

__attribute__((swift_name("KotlinNumber")))
@interface PIAANumber : NSNumber
- (instancetype)initWithChar:(char)value __attribute__((unavailable));
- (instancetype)initWithUnsignedChar:(unsigned char)value __attribute__((unavailable));
- (instancetype)initWithShort:(short)value __attribute__((unavailable));
- (instancetype)initWithUnsignedShort:(unsigned short)value __attribute__((unavailable));
- (instancetype)initWithInt:(int)value __attribute__((unavailable));
- (instancetype)initWithUnsignedInt:(unsigned int)value __attribute__((unavailable));
- (instancetype)initWithLong:(long)value __attribute__((unavailable));
- (instancetype)initWithUnsignedLong:(unsigned long)value __attribute__((unavailable));
- (instancetype)initWithLongLong:(long long)value __attribute__((unavailable));
- (instancetype)initWithUnsignedLongLong:(unsigned long long)value __attribute__((unavailable));
- (instancetype)initWithFloat:(float)value __attribute__((unavailable));
- (instancetype)initWithDouble:(double)value __attribute__((unavailable));
- (instancetype)initWithBool:(BOOL)value __attribute__((unavailable));
- (instancetype)initWithInteger:(NSInteger)value __attribute__((unavailable));
- (instancetype)initWithUnsignedInteger:(NSUInteger)value __attribute__((unavailable));
+ (instancetype)numberWithChar:(char)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedChar:(unsigned char)value __attribute__((unavailable));
+ (instancetype)numberWithShort:(short)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedShort:(unsigned short)value __attribute__((unavailable));
+ (instancetype)numberWithInt:(int)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedInt:(unsigned int)value __attribute__((unavailable));
+ (instancetype)numberWithLong:(long)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedLong:(unsigned long)value __attribute__((unavailable));
+ (instancetype)numberWithLongLong:(long long)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedLongLong:(unsigned long long)value __attribute__((unavailable));
+ (instancetype)numberWithFloat:(float)value __attribute__((unavailable));
+ (instancetype)numberWithDouble:(double)value __attribute__((unavailable));
+ (instancetype)numberWithBool:(BOOL)value __attribute__((unavailable));
+ (instancetype)numberWithInteger:(NSInteger)value __attribute__((unavailable));
+ (instancetype)numberWithUnsignedInteger:(NSUInteger)value __attribute__((unavailable));
@end;

__attribute__((swift_name("KotlinByte")))
@interface PIAAByte : PIAANumber
- (instancetype)initWithChar:(char)value;
+ (instancetype)numberWithChar:(char)value;
@end;

__attribute__((swift_name("KotlinUByte")))
@interface PIAAUByte : PIAANumber
- (instancetype)initWithUnsignedChar:(unsigned char)value;
+ (instancetype)numberWithUnsignedChar:(unsigned char)value;
@end;

__attribute__((swift_name("KotlinShort")))
@interface PIAAShort : PIAANumber
- (instancetype)initWithShort:(short)value;
+ (instancetype)numberWithShort:(short)value;
@end;

__attribute__((swift_name("KotlinUShort")))
@interface PIAAUShort : PIAANumber
- (instancetype)initWithUnsignedShort:(unsigned short)value;
+ (instancetype)numberWithUnsignedShort:(unsigned short)value;
@end;

__attribute__((swift_name("KotlinInt")))
@interface PIAAInt : PIAANumber
- (instancetype)initWithInt:(int)value;
+ (instancetype)numberWithInt:(int)value;
@end;

__attribute__((swift_name("KotlinUInt")))
@interface PIAAUInt : PIAANumber
- (instancetype)initWithUnsignedInt:(unsigned int)value;
+ (instancetype)numberWithUnsignedInt:(unsigned int)value;
@end;

__attribute__((swift_name("KotlinLong")))
@interface PIAALong : PIAANumber
- (instancetype)initWithLongLong:(long long)value;
+ (instancetype)numberWithLongLong:(long long)value;
@end;

__attribute__((swift_name("KotlinULong")))
@interface PIAAULong : PIAANumber
- (instancetype)initWithUnsignedLongLong:(unsigned long long)value;
+ (instancetype)numberWithUnsignedLongLong:(unsigned long long)value;
@end;

__attribute__((swift_name("KotlinFloat")))
@interface PIAAFloat : PIAANumber
- (instancetype)initWithFloat:(float)value;
+ (instancetype)numberWithFloat:(float)value;
@end;

__attribute__((swift_name("KotlinDouble")))
@interface PIAADouble : PIAANumber
- (instancetype)initWithDouble:(double)value;
+ (instancetype)numberWithDouble:(double)value;
@end;

__attribute__((swift_name("KotlinBoolean")))
@interface PIAABoolean : PIAANumber
- (instancetype)initWithBool:(BOOL)value;
+ (instancetype)numberWithBool:(BOOL)value;
@end;

__attribute__((swift_name("AccountAPI")))
@protocol PIAAAccountAPI
@required
- (void)accountDetailsToken:(NSString *)token callback:(void (^)(PIAAAccountInformation * _Nullable, PIAAAccountRequestError * _Nullable))callback __attribute__((swift_name("accountDetails(token:callback:)")));
- (void)clientStatusCallback:(void (^)(PIAAClientStatusInformation * _Nullable, PIAAAccountRequestError * _Nullable))callback __attribute__((swift_name("clientStatus(callback:)")));
- (void)invitesDetailsToken:(NSString *)token callback:(void (^)(PIAAInvitesDetailsInformation * _Nullable, PIAAAccountRequestError * _Nullable))callback __attribute__((swift_name("invitesDetails(token:callback:)")));
- (BOOL)isStaging __attribute__((swift_name("isStaging()")));
- (void)loginLinkEmail:(NSString *)email callback:(void (^)(PIAAAccountRequestError * _Nullable))callback __attribute__((swift_name("loginLink(email:callback:)")));
- (void)loginWithCredentialsUsername:(NSString *)username password:(NSString *)password callback:(void (^)(NSString * _Nullable, PIAAAccountRequestError * _Nullable))callback __attribute__((swift_name("loginWithCredentials(username:password:callback:)")));
- (void)logoutToken:(NSString *)token callback:(void (^)(PIAAAccountRequestError * _Nullable))callback __attribute__((swift_name("logout(token:callback:)")));
- (void)redeemEmail:(NSString *)email code:(NSString *)code callback:(void (^)(PIAARedeemInformation * _Nullable, PIAAAccountRequestError * _Nullable))callback __attribute__((swift_name("redeem(email:code:callback:)")));
- (void)sendInviteToken:(NSString *)token recipientEmail:(NSString *)recipientEmail recipientName:(NSString *)recipientName callback:(void (^)(PIAAAccountRequestError * _Nullable))callback __attribute__((swift_name("sendInvite(token:recipientEmail:recipientName:callback:)")));
- (void)setEmailToken:(NSString *)token email:(NSString *)email resetPassword:(BOOL)resetPassword callback:(void (^)(NSString * _Nullable, PIAAAccountRequestError * _Nullable))callback __attribute__((swift_name("setEmail(token:email:resetPassword:callback:)")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AccountBuilder")))
@interface PIAAAccountBuilder : PIAABase
- (instancetype)init __attribute__((swift_name("init()"))) __attribute__((objc_designated_initializer));
+ (instancetype)new __attribute__((availability(swift, unavailable, message="use object initializers instead")));
- (id _Nullable)build __attribute__((swift_name("build()")));
- (PIAAAccountBuilder *)setPlatformPlatform:(PIAAPlatform *)platform __attribute__((swift_name("setPlatform(platform:)")));
- (PIAAAccountBuilder *)setStagingEndpointStagingEndpoint:(NSString *)stagingEndpoint __attribute__((swift_name("setStagingEndpoint(stagingEndpoint:)")));
- (PIAAAccountBuilder *)setUserAgentValueUserAgentValue:(NSString *)userAgentValue __attribute__((swift_name("setUserAgentValue(userAgentValue:)")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AccountRequestError")))
@interface PIAAAccountRequestError : PIAABase
- (instancetype)initWithCode:(int32_t)code message:(NSString * _Nullable)message __attribute__((swift_name("init(code:message:)"))) __attribute__((objc_designated_initializer));
- (int32_t)component1 __attribute__((swift_name("component1()")));
- (NSString * _Nullable)component2 __attribute__((swift_name("component2()")));
- (PIAAAccountRequestError *)doCopyCode:(int32_t)code message:(NSString * _Nullable)message __attribute__((swift_name("doCopy(code:message:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) int32_t code __attribute__((swift_name("code")));
@property (readonly) NSString * _Nullable message __attribute__((swift_name("message")));
@end;

__attribute__((swift_name("AndroidAccountAPI")))
@protocol PIAAAndroidAccountAPI <PIAAAccountAPI>
@required
- (void)loginWithReceiptStore:(NSString *)store token:(NSString *)token productId:(NSString *)productId applicationPackage:(NSString *)applicationPackage callback:(void (^)(NSString * _Nullable, PIAAAccountRequestError * _Nullable))callback __attribute__((swift_name("loginWithReceipt(store:token:productId:applicationPackage:callback:)")));
- (void)signUpInformation:(PIAAAndroidSignupInformation *)information callback:(void (^)(PIAASignUpInformation * _Nullable, PIAAAccountRequestError * _Nullable))callback __attribute__((swift_name("signUp(information:callback:)")));
- (void)subscriptionsCallback:(void (^)(PIAAAndroidSubscriptionsInformation * _Nullable, PIAAAccountRequestError * _Nullable))callback __attribute__((swift_name("subscriptions(callback:)")));
@end;

__attribute__((swift_name("IOSAccountAPI")))
@protocol PIAAIOSAccountAPI <PIAAAccountAPI>
@required
- (void)loginWithReceiptReceiptBase64:(NSString *)receiptBase64 callback:(void (^)(NSString * _Nullable, PIAAAccountRequestError * _Nullable))callback __attribute__((swift_name("loginWithReceipt(receiptBase64:callback:)")));
- (void)paymentUsername:(NSString *)username password:(NSString *)password information:(PIAAIOSPaymentInformation *)information callback:(void (^)(PIAAAccountRequestError * _Nullable))callback __attribute__((swift_name("payment(username:password:information:callback:)")));
- (void)setEmailUsername:(NSString *)username password:(NSString *)password email:(NSString *)email resetPassword:(BOOL)resetPassword callback:(void (^)(NSString * _Nullable, PIAAAccountRequestError * _Nullable))callback __attribute__((swift_name("setEmail(username:password:email:resetPassword:callback:)")));
- (void)signUpInformation:(PIAAIOSSignupInformation *)information callback_:(void (^)(PIAASignUpInformation * _Nullable, PIAAAccountRequestError * _Nullable))callback __attribute__((swift_name("signUp(information:callback_:)")));
- (void)subscriptionsReceipt:(NSString * _Nullable)receipt callback:(void (^)(PIAAIOSSubscriptionInformation * _Nullable, PIAAAccountRequestError * _Nullable))callback __attribute__((swift_name("subscriptions(receipt:callback:)")));
@end;

__attribute__((swift_name("KotlinComparable")))
@protocol PIAAKotlinComparable
@required
- (int32_t)compareToOther:(id _Nullable)other __attribute__((swift_name("compareTo(other:)")));
@end;

__attribute__((swift_name("KotlinEnum")))
@interface PIAAKotlinEnum : PIAABase <PIAAKotlinComparable>
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer));
- (int32_t)compareToOther:(PIAAKotlinEnum *)other __attribute__((swift_name("compareTo(other:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *name __attribute__((swift_name("name")));
@property (readonly) int32_t ordinal __attribute__((swift_name("ordinal")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Platform")))
@interface PIAAPlatform : PIAAKotlinEnum
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) PIAAPlatform *ios __attribute__((swift_name("ios")));
@property (class, readonly) PIAAPlatform *android __attribute__((swift_name("android")));
- (int32_t)compareToOther:(PIAAPlatform *)other __attribute__((swift_name("compareTo(other:)")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AccountUtils")))
@interface PIAAAccountUtils : PIAABase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)accountUtils __attribute__((swift_name("init()")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AndroidSignupInformation")))
@interface PIAAAndroidSignupInformation : PIAABase
- (instancetype)initWithStore:(NSString *)store receipt:(PIAAAndroidSignupInformationReceipt *)receipt marketing:(NSString * _Nullable)marketing __attribute__((swift_name("init(store:receipt:marketing:)"))) __attribute__((objc_designated_initializer));
- (PIAAAndroidSignupInformationReceipt *)component2 __attribute__((swift_name("component2()")));
- (NSString * _Nullable)component3 __attribute__((swift_name("component3()")));
- (PIAAAndroidSignupInformation *)doCopyStore:(NSString *)store receipt:(PIAAAndroidSignupInformationReceipt *)receipt marketing:(NSString * _Nullable)marketing __attribute__((swift_name("doCopy(store:receipt:marketing:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString * _Nullable marketing __attribute__((swift_name("marketing")));
@property (readonly) PIAAAndroidSignupInformationReceipt *receipt __attribute__((swift_name("receipt")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AndroidSignupInformation.Companion")))
@interface PIAAAndroidSignupInformationCompanion : PIAABase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
- (id<PIAAKotlinx_serialization_runtimeKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AndroidSignupInformation.Receipt")))
@interface PIAAAndroidSignupInformationReceipt : PIAABase
- (instancetype)initWithOrderId:(NSString *)orderId token:(NSString *)token sku:(NSString *)sku __attribute__((swift_name("init(orderId:token:sku:)"))) __attribute__((objc_designated_initializer));
- (NSString *)component1 __attribute__((swift_name("component1()")));
- (NSString *)component2 __attribute__((swift_name("component2()")));
- (NSString *)component3 __attribute__((swift_name("component3()")));
- (PIAAAndroidSignupInformationReceipt *)doCopyOrderId:(NSString *)orderId token:(NSString *)token sku:(NSString *)sku __attribute__((swift_name("doCopy(orderId:token:sku:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *orderId __attribute__((swift_name("orderId")));
@property (readonly) NSString *sku __attribute__((swift_name("sku")));
@property (readonly) NSString *token __attribute__((swift_name("token")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AndroidSignupInformation.ReceiptCompanion")))
@interface PIAAAndroidSignupInformationReceiptCompanion : PIAABase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
- (id<PIAAKotlinx_serialization_runtimeKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("IOSPaymentInformation")))
@interface PIAAIOSPaymentInformation : PIAABase
- (instancetype)initWithStore:(NSString *)store receipt:(NSString *)receipt marketing:(NSString *)marketing debug:(NSString *)debug __attribute__((swift_name("init(store:receipt:marketing:debug:)"))) __attribute__((objc_designated_initializer));
- (NSString *)component2 __attribute__((swift_name("component2()")));
- (NSString *)component3 __attribute__((swift_name("component3()")));
- (NSString *)component4 __attribute__((swift_name("component4()")));
- (PIAAIOSPaymentInformation *)doCopyStore:(NSString *)store receipt:(NSString *)receipt marketing:(NSString *)marketing debug:(NSString *)debug __attribute__((swift_name("doCopy(store:receipt:marketing:debug:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *debug __attribute__((swift_name("debug")));
@property (readonly) NSString *marketing __attribute__((swift_name("marketing")));
@property (readonly) NSString *receipt __attribute__((swift_name("receipt")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("IOSPaymentInformation.Companion")))
@interface PIAAIOSPaymentInformationCompanion : PIAABase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
- (id<PIAAKotlinx_serialization_runtimeKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("IOSSignupInformation")))
@interface PIAAIOSSignupInformation : PIAABase
- (instancetype)initWithStore:(NSString *)store receipt:(NSString *)receipt email:(NSString *)email marketing:(NSString * _Nullable)marketing debug:(NSString * _Nullable)debug __attribute__((swift_name("init(store:receipt:email:marketing:debug:)"))) __attribute__((objc_designated_initializer));
- (NSString *)component2 __attribute__((swift_name("component2()")));
- (NSString *)component3 __attribute__((swift_name("component3()")));
- (NSString * _Nullable)component4 __attribute__((swift_name("component4()")));
- (NSString * _Nullable)component5 __attribute__((swift_name("component5()")));
- (PIAAIOSSignupInformation *)doCopyStore:(NSString *)store receipt:(NSString *)receipt email:(NSString *)email marketing:(NSString * _Nullable)marketing debug:(NSString * _Nullable)debug __attribute__((swift_name("doCopy(store:receipt:email:marketing:debug:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString * _Nullable debug __attribute__((swift_name("debug")));
@property (readonly) NSString *email __attribute__((swift_name("email")));
@property (readonly) NSString * _Nullable marketing __attribute__((swift_name("marketing")));
@property (readonly) NSString *receipt __attribute__((swift_name("receipt")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("IOSSignupInformation.Companion")))
@interface PIAAIOSSignupInformationCompanion : PIAABase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
- (id<PIAAKotlinx_serialization_runtimeKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AccountInformation")))
@interface PIAAAccountInformation : PIAABase
- (instancetype)initWithActive:(BOOL)active canInvite:(BOOL)canInvite canceled:(BOOL)canceled daysRemaining:(int32_t)daysRemaining email:(NSString *)email expirationTime:(int32_t)expirationTime expireAlert:(BOOL)expireAlert expired:(BOOL)expired needsPayment:(BOOL)needsPayment plan:(NSString *)plan productId:(NSString * _Nullable)productId recurring:(BOOL)recurring renewUrl:(NSString *)renewUrl renewable:(BOOL)renewable username:(NSString *)username __attribute__((swift_name("init(active:canInvite:canceled:daysRemaining:email:expirationTime:expireAlert:expired:needsPayment:plan:productId:recurring:renewUrl:renewable:username:)"))) __attribute__((objc_designated_initializer));
- (BOOL)component1 __attribute__((swift_name("component1()")));
- (NSString *)component10 __attribute__((swift_name("component10()")));
- (NSString * _Nullable)component11 __attribute__((swift_name("component11()")));
- (BOOL)component12 __attribute__((swift_name("component12()")));
- (NSString *)component13 __attribute__((swift_name("component13()")));
- (BOOL)component14 __attribute__((swift_name("component14()")));
- (NSString *)component15 __attribute__((swift_name("component15()")));
- (BOOL)component2 __attribute__((swift_name("component2()")));
- (BOOL)component3 __attribute__((swift_name("component3()")));
- (int32_t)component4 __attribute__((swift_name("component4()")));
- (NSString *)component5 __attribute__((swift_name("component5()")));
- (int32_t)component6 __attribute__((swift_name("component6()")));
- (BOOL)component7 __attribute__((swift_name("component7()")));
- (BOOL)component8 __attribute__((swift_name("component8()")));
- (BOOL)component9 __attribute__((swift_name("component9()")));
- (PIAAAccountInformation *)doCopyActive:(BOOL)active canInvite:(BOOL)canInvite canceled:(BOOL)canceled daysRemaining:(int32_t)daysRemaining email:(NSString *)email expirationTime:(int32_t)expirationTime expireAlert:(BOOL)expireAlert expired:(BOOL)expired needsPayment:(BOOL)needsPayment plan:(NSString *)plan productId:(NSString * _Nullable)productId recurring:(BOOL)recurring renewUrl:(NSString *)renewUrl renewable:(BOOL)renewable username:(NSString *)username __attribute__((swift_name("doCopy(active:canInvite:canceled:daysRemaining:email:expirationTime:expireAlert:expired:needsPayment:plan:productId:recurring:renewUrl:renewable:username:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) BOOL active __attribute__((swift_name("active")));
@property (readonly) BOOL canInvite __attribute__((swift_name("canInvite")));
@property (readonly) BOOL canceled __attribute__((swift_name("canceled")));
@property (readonly) int32_t daysRemaining __attribute__((swift_name("daysRemaining")));
@property (readonly) NSString *email __attribute__((swift_name("email")));
@property (readonly) int32_t expirationTime __attribute__((swift_name("expirationTime")));
@property (readonly) BOOL expireAlert __attribute__((swift_name("expireAlert")));
@property (readonly) BOOL expired __attribute__((swift_name("expired")));
@property (readonly) BOOL needsPayment __attribute__((swift_name("needsPayment")));
@property (readonly) NSString *plan __attribute__((swift_name("plan")));
@property (readonly) NSString * _Nullable productId __attribute__((swift_name("productId")));
@property (readonly) BOOL recurring __attribute__((swift_name("recurring")));
@property (readonly) NSString *renewUrl __attribute__((swift_name("renewUrl")));
@property (readonly) BOOL renewable __attribute__((swift_name("renewable")));
@property (readonly) NSString *username __attribute__((swift_name("username")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AccountInformation.Companion")))
@interface PIAAAccountInformationCompanion : PIAABase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
- (id<PIAAKotlinx_serialization_runtimeKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AndroidSubscriptionsInformation")))
@interface PIAAAndroidSubscriptionsInformation : PIAABase
- (instancetype)initWithAvailableProducts:(NSArray<PIAAAndroidSubscriptionsInformationAvailableProduct *> *)availableProducts status:(NSString *)status __attribute__((swift_name("init(availableProducts:status:)"))) __attribute__((objc_designated_initializer));
- (NSArray<PIAAAndroidSubscriptionsInformationAvailableProduct *> *)component1 __attribute__((swift_name("component1()")));
- (NSString *)component2 __attribute__((swift_name("component2()")));
- (PIAAAndroidSubscriptionsInformation *)doCopyAvailableProducts:(NSArray<PIAAAndroidSubscriptionsInformationAvailableProduct *> *)availableProducts status:(NSString *)status __attribute__((swift_name("doCopy(availableProducts:status:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSArray<PIAAAndroidSubscriptionsInformationAvailableProduct *> *availableProducts __attribute__((swift_name("availableProducts")));
@property (readonly) NSString *status __attribute__((swift_name("status")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AndroidSubscriptionsInformation.AvailableProduct")))
@interface PIAAAndroidSubscriptionsInformationAvailableProduct : PIAABase
- (instancetype)initWithId:(NSString *)id legacy:(BOOL)legacy plan:(NSString *)plan price:(NSString *)price __attribute__((swift_name("init(id:legacy:plan:price:)"))) __attribute__((objc_designated_initializer));
- (NSString *)component1 __attribute__((swift_name("component1()")));
- (BOOL)component2 __attribute__((swift_name("component2()")));
- (NSString *)component3 __attribute__((swift_name("component3()")));
- (NSString *)component4 __attribute__((swift_name("component4()")));
- (PIAAAndroidSubscriptionsInformationAvailableProduct *)doCopyId:(NSString *)id legacy:(BOOL)legacy plan:(NSString *)plan price:(NSString *)price __attribute__((swift_name("doCopy(id:legacy:plan:price:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *id __attribute__((swift_name("id")));
@property (readonly) BOOL legacy __attribute__((swift_name("legacy")));
@property (readonly) NSString *plan __attribute__((swift_name("plan")));
@property (readonly) NSString *price __attribute__((swift_name("price")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AndroidSubscriptionsInformation.AvailableProductCompanion")))
@interface PIAAAndroidSubscriptionsInformationAvailableProductCompanion : PIAABase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
- (id<PIAAKotlinx_serialization_runtimeKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("AndroidSubscriptionsInformation.Companion")))
@interface PIAAAndroidSubscriptionsInformationCompanion : PIAABase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
- (id<PIAAKotlinx_serialization_runtimeKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ClientStatusInformation")))
@interface PIAAClientStatusInformation : PIAABase
- (instancetype)initWithConnected:(BOOL)connected ip:(NSString *)ip __attribute__((swift_name("init(connected:ip:)"))) __attribute__((objc_designated_initializer));
- (BOOL)component1 __attribute__((swift_name("component1()")));
- (NSString *)component2 __attribute__((swift_name("component2()")));
- (PIAAClientStatusInformation *)doCopyConnected:(BOOL)connected ip:(NSString *)ip __attribute__((swift_name("doCopy(connected:ip:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) BOOL connected __attribute__((swift_name("connected")));
@property (readonly) NSString *ip __attribute__((swift_name("ip")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("ClientStatusInformation.Companion")))
@interface PIAAClientStatusInformationCompanion : PIAABase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
- (id<PIAAKotlinx_serialization_runtimeKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("IOSSubscriptionInformation")))
@interface PIAAIOSSubscriptionInformation : PIAABase
- (instancetype)initWithAvailableProducts:(NSArray<PIAAIOSSubscriptionInformationAvailableProduct *> *)availableProducts eligibleForTrial:(BOOL)eligibleForTrial receipt:(PIAAIOSSubscriptionInformationReceipt *)receipt status:(NSString *)status __attribute__((swift_name("init(availableProducts:eligibleForTrial:receipt:status:)"))) __attribute__((objc_designated_initializer));
- (NSArray<PIAAIOSSubscriptionInformationAvailableProduct *> *)component1 __attribute__((swift_name("component1()")));
- (BOOL)component2 __attribute__((swift_name("component2()")));
- (PIAAIOSSubscriptionInformationReceipt *)component3 __attribute__((swift_name("component3()")));
- (NSString *)component4 __attribute__((swift_name("component4()")));
- (PIAAIOSSubscriptionInformation *)doCopyAvailableProducts:(NSArray<PIAAIOSSubscriptionInformationAvailableProduct *> *)availableProducts eligibleForTrial:(BOOL)eligibleForTrial receipt:(PIAAIOSSubscriptionInformationReceipt *)receipt status:(NSString *)status __attribute__((swift_name("doCopy(availableProducts:eligibleForTrial:receipt:status:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSArray<PIAAIOSSubscriptionInformationAvailableProduct *> *availableProducts __attribute__((swift_name("availableProducts")));
@property (readonly) BOOL eligibleForTrial __attribute__((swift_name("eligibleForTrial")));
@property (readonly) PIAAIOSSubscriptionInformationReceipt *receipt __attribute__((swift_name("receipt")));
@property (readonly) NSString *status __attribute__((swift_name("status")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("IOSSubscriptionInformation.AvailableProduct")))
@interface PIAAIOSSubscriptionInformationAvailableProduct : PIAABase
- (instancetype)initWithId:(NSString *)id legacy:(BOOL)legacy plan:(NSString *)plan price:(NSString *)price __attribute__((swift_name("init(id:legacy:plan:price:)"))) __attribute__((objc_designated_initializer));
- (NSString *)component1 __attribute__((swift_name("component1()")));
- (BOOL)component2 __attribute__((swift_name("component2()")));
- (NSString *)component3 __attribute__((swift_name("component3()")));
- (NSString *)component4 __attribute__((swift_name("component4()")));
- (PIAAIOSSubscriptionInformationAvailableProduct *)doCopyId:(NSString *)id legacy:(BOOL)legacy plan:(NSString *)plan price:(NSString *)price __attribute__((swift_name("doCopy(id:legacy:plan:price:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSString *id __attribute__((swift_name("id")));
@property (readonly) BOOL legacy __attribute__((swift_name("legacy")));
@property (readonly) NSString *plan __attribute__((swift_name("plan")));
@property (readonly) NSString *price __attribute__((swift_name("price")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("IOSSubscriptionInformation.AvailableProductCompanion")))
@interface PIAAIOSSubscriptionInformationAvailableProductCompanion : PIAABase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
- (id<PIAAKotlinx_serialization_runtimeKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("IOSSubscriptionInformation.Companion")))
@interface PIAAIOSSubscriptionInformationCompanion : PIAABase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
- (id<PIAAKotlinx_serialization_runtimeKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("IOSSubscriptionInformation.Receipt")))
@interface PIAAIOSSubscriptionInformationReceipt : PIAABase
- (instancetype)initWithEligibleForTrial:(BOOL)eligibleForTrial __attribute__((swift_name("init(eligibleForTrial:)"))) __attribute__((objc_designated_initializer));
- (BOOL)component1 __attribute__((swift_name("component1()")));
- (PIAAIOSSubscriptionInformationReceipt *)doCopyEligibleForTrial:(BOOL)eligibleForTrial __attribute__((swift_name("doCopy(eligibleForTrial:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) BOOL eligibleForTrial __attribute__((swift_name("eligibleForTrial")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("IOSSubscriptionInformation.ReceiptCompanion")))
@interface PIAAIOSSubscriptionInformationReceiptCompanion : PIAABase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
- (id<PIAAKotlinx_serialization_runtimeKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("InvitesDetailsInformation")))
@interface PIAAInvitesDetailsInformation : PIAABase
- (instancetype)initWithInvites:(NSArray<PIAAInvitesDetailsInformationInvite *> *)invites totalFreeDaysGiven:(int32_t)totalFreeDaysGiven totalInvitesRewarded:(int32_t)totalInvitesRewarded totalInvitesSent:(int32_t)totalInvitesSent uniqueReferralLink:(NSString *)uniqueReferralLink __attribute__((swift_name("init(invites:totalFreeDaysGiven:totalInvitesRewarded:totalInvitesSent:uniqueReferralLink:)"))) __attribute__((objc_designated_initializer));
- (NSArray<PIAAInvitesDetailsInformationInvite *> *)component1 __attribute__((swift_name("component1()")));
- (int32_t)component2 __attribute__((swift_name("component2()")));
- (int32_t)component3 __attribute__((swift_name("component3()")));
- (int32_t)component4 __attribute__((swift_name("component4()")));
- (NSString *)component5 __attribute__((swift_name("component5()")));
- (PIAAInvitesDetailsInformation *)doCopyInvites:(NSArray<PIAAInvitesDetailsInformationInvite *> *)invites totalFreeDaysGiven:(int32_t)totalFreeDaysGiven totalInvitesRewarded:(int32_t)totalInvitesRewarded totalInvitesSent:(int32_t)totalInvitesSent uniqueReferralLink:(NSString *)uniqueReferralLink __attribute__((swift_name("doCopy(invites:totalFreeDaysGiven:totalInvitesRewarded:totalInvitesSent:uniqueReferralLink:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) NSArray<PIAAInvitesDetailsInformationInvite *> *invites __attribute__((swift_name("invites")));
@property (readonly) int32_t totalFreeDaysGiven __attribute__((swift_name("totalFreeDaysGiven")));
@property (readonly) int32_t totalInvitesRewarded __attribute__((swift_name("totalInvitesRewarded")));
@property (readonly) int32_t totalInvitesSent __attribute__((swift_name("totalInvitesSent")));
@property (readonly) NSString *uniqueReferralLink __attribute__((swift_name("uniqueReferralLink")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("InvitesDetailsInformation.Companion")))
@interface PIAAInvitesDetailsInformationCompanion : PIAABase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
- (id<PIAAKotlinx_serialization_runtimeKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("InvitesDetailsInformation.Invite")))
@interface PIAAInvitesDetailsInformationInvite : PIAABase
- (instancetype)initWithAccepted:(BOOL)accepted gracePeriodRemaining:(NSString *)gracePeriodRemaining obfuscatedEmail:(NSString *)obfuscatedEmail rewarded:(BOOL)rewarded __attribute__((swift_name("init(accepted:gracePeriodRemaining:obfuscatedEmail:rewarded:)"))) __attribute__((objc_designated_initializer));
- (BOOL)component1 __attribute__((swift_name("component1()")));
- (NSString *)component2 __attribute__((swift_name("component2()")));
- (NSString *)component3 __attribute__((swift_name("component3()")));
- (BOOL)component4 __attribute__((swift_name("component4()")));
- (PIAAInvitesDetailsInformationInvite *)doCopyAccepted:(BOOL)accepted gracePeriodRemaining:(NSString *)gracePeriodRemaining obfuscatedEmail:(NSString *)obfuscatedEmail rewarded:(BOOL)rewarded __attribute__((swift_name("doCopy(accepted:gracePeriodRemaining:obfuscatedEmail:rewarded:)")));
- (BOOL)isEqual:(id _Nullable)other __attribute__((swift_name("isEqual(_:)")));
- (NSUInteger)hash __attribute__((swift_name("hash()")));
- (NSString *)description __attribute__((swift_name("description()")));
@property (readonly) BOOL accepted __attribute__((swift_name("accepted")));
@property (readonly) NSString *gracePeriodRemaining __attribute__((swift_name("gracePeriodRemaining")));
@property (readonly) NSString *obfuscatedEmail __attribute__((swift_name("obfuscatedEmail")));
@property (readonly) BOOL rewarded __attribute__((swift_name("rewarded")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("InvitesDetailsInformation.InviteCompanion")))
@interface PIAAInvitesDetailsInformationInviteCompanion : PIAABase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
- (id<PIAAKotlinx_serialization_runtimeKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("RedeemInformation")))
@interface PIAARedeemInformation : PIAABase
- (instancetype)initWithMessage:(NSString * _Nullable)message username:(NSString *)username password:(NSString *)password __attribute__((swift_name("init(message:username:password:)"))) __attribute__((objc_designated_initializer));
@property (readonly) NSString * _Nullable message __attribute__((swift_name("message")));
@property (readonly) NSString *password __attribute__((swift_name("password")));
@property (readonly) NSString *username __attribute__((swift_name("username")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("RedeemInformation.Companion")))
@interface PIAARedeemInformationCompanion : PIAABase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
- (id<PIAAKotlinx_serialization_runtimeKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SignUpInformation")))
@interface PIAASignUpInformation : PIAABase
- (instancetype)initWithStatus:(NSString *)status username:(NSString *)username password:(NSString *)password __attribute__((swift_name("init(status:username:password:)"))) __attribute__((objc_designated_initializer));
@property (readonly) NSString *password __attribute__((swift_name("password")));
@property (readonly) NSString *status __attribute__((swift_name("status")));
@property (readonly) NSString *username __attribute__((swift_name("username")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("SignUpInformation.Companion")))
@interface PIAASignUpInformationCompanion : PIAABase
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
+ (instancetype)companion __attribute__((swift_name("init()")));
- (id<PIAAKotlinx_serialization_runtimeKSerializer>)serializer __attribute__((swift_name("serializer()")));
@end;

__attribute__((swift_name("Kotlinx_serialization_runtimeSerializationStrategy")))
@protocol PIAAKotlinx_serialization_runtimeSerializationStrategy
@required
- (void)serializeEncoder:(id<PIAAKotlinx_serialization_runtimeEncoder>)encoder value:(id _Nullable)value __attribute__((swift_name("serialize(encoder:value:)")));
@property (readonly) id<PIAAKotlinx_serialization_runtimeSerialDescriptor> descriptor __attribute__((swift_name("descriptor")));
@end;

__attribute__((swift_name("Kotlinx_serialization_runtimeDeserializationStrategy")))
@protocol PIAAKotlinx_serialization_runtimeDeserializationStrategy
@required
- (id _Nullable)deserializeDecoder:(id<PIAAKotlinx_serialization_runtimeDecoder>)decoder __attribute__((swift_name("deserialize(decoder:)")));
- (id _Nullable)patchDecoder:(id<PIAAKotlinx_serialization_runtimeDecoder>)decoder old:(id _Nullable)old __attribute__((swift_name("patch(decoder:old:)")));
@property (readonly) id<PIAAKotlinx_serialization_runtimeSerialDescriptor> descriptor __attribute__((swift_name("descriptor")));
@end;

__attribute__((swift_name("Kotlinx_serialization_runtimeKSerializer")))
@protocol PIAAKotlinx_serialization_runtimeKSerializer <PIAAKotlinx_serialization_runtimeSerializationStrategy, PIAAKotlinx_serialization_runtimeDeserializationStrategy>
@required
@end;

__attribute__((swift_name("Kotlinx_serialization_runtimeEncoder")))
@protocol PIAAKotlinx_serialization_runtimeEncoder
@required
- (id<PIAAKotlinx_serialization_runtimeCompositeEncoder>)beginCollectionDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor collectionSize:(int32_t)collectionSize typeSerializers:(PIAAKotlinArray *)typeSerializers __attribute__((swift_name("beginCollection(descriptor:collectionSize:typeSerializers:)")));
- (id<PIAAKotlinx_serialization_runtimeCompositeEncoder>)beginStructureDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor typeSerializers:(PIAAKotlinArray *)typeSerializers __attribute__((swift_name("beginStructure(descriptor:typeSerializers:)")));
- (void)encodeBooleanValue:(BOOL)value __attribute__((swift_name("encodeBoolean(value:)")));
- (void)encodeByteValue:(int8_t)value __attribute__((swift_name("encodeByte(value:)")));
- (void)encodeCharValue:(unichar)value __attribute__((swift_name("encodeChar(value:)")));
- (void)encodeDoubleValue:(double)value __attribute__((swift_name("encodeDouble(value:)")));
- (void)encodeEnumEnumDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)enumDescriptor index:(int32_t)index __attribute__((swift_name("encodeEnum(enumDescriptor:index:)")));
- (void)encodeFloatValue:(float)value __attribute__((swift_name("encodeFloat(value:)")));
- (void)encodeIntValue:(int32_t)value __attribute__((swift_name("encodeInt(value:)")));
- (void)encodeLongValue:(int64_t)value __attribute__((swift_name("encodeLong(value:)")));
- (void)encodeNotNullMark __attribute__((swift_name("encodeNotNullMark()")));
- (void)encodeNull __attribute__((swift_name("encodeNull()")));
- (void)encodeNullableSerializableValueSerializer:(id<PIAAKotlinx_serialization_runtimeSerializationStrategy>)serializer value:(id _Nullable)value __attribute__((swift_name("encodeNullableSerializableValue(serializer:value:)")));
- (void)encodeSerializableValueSerializer:(id<PIAAKotlinx_serialization_runtimeSerializationStrategy>)serializer value:(id _Nullable)value __attribute__((swift_name("encodeSerializableValue(serializer:value:)")));
- (void)encodeShortValue:(int16_t)value __attribute__((swift_name("encodeShort(value:)")));
- (void)encodeStringValue:(NSString *)value __attribute__((swift_name("encodeString(value:)")));
- (void)encodeUnit __attribute__((swift_name("encodeUnit()")));
@property (readonly) id<PIAAKotlinx_serialization_runtimeSerialModule> context __attribute__((swift_name("context")));
@end;

__attribute__((swift_name("Kotlinx_serialization_runtimeSerialDescriptor")))
@protocol PIAAKotlinx_serialization_runtimeSerialDescriptor
@required
- (NSArray<id<PIAAKotlinAnnotation>> *)getElementAnnotationsIndex:(int32_t)index __attribute__((swift_name("getElementAnnotations(index:)")));
- (id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)getElementDescriptorIndex:(int32_t)index __attribute__((swift_name("getElementDescriptor(index:)")));
- (int32_t)getElementIndexName:(NSString *)name __attribute__((swift_name("getElementIndex(name:)")));
- (NSString *)getElementNameIndex:(int32_t)index __attribute__((swift_name("getElementName(index:)")));
- (NSArray<id<PIAAKotlinAnnotation>> *)getEntityAnnotations __attribute__((swift_name("getEntityAnnotations()"))) __attribute__((deprecated("Deprecated in the favour of 'annotations' property")));
- (BOOL)isElementOptionalIndex:(int32_t)index __attribute__((swift_name("isElementOptional(index:)")));
@property (readonly) NSArray<id<PIAAKotlinAnnotation>> *annotations __attribute__((swift_name("annotations")));
@property (readonly) int32_t elementsCount __attribute__((swift_name("elementsCount")));
@property (readonly) BOOL isNullable __attribute__((swift_name("isNullable")));
@property (readonly) PIAAKotlinx_serialization_runtimeSerialKind *kind __attribute__((swift_name("kind")));
@property (readonly) NSString *name __attribute__((swift_name("name"))) __attribute__((unavailable("name property deprecated in the favour of serialName")));
@property (readonly) NSString *serialName __attribute__((swift_name("serialName")));
@end;

__attribute__((swift_name("Kotlinx_serialization_runtimeDecoder")))
@protocol PIAAKotlinx_serialization_runtimeDecoder
@required
- (id<PIAAKotlinx_serialization_runtimeCompositeDecoder>)beginStructureDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor typeParams:(PIAAKotlinArray *)typeParams __attribute__((swift_name("beginStructure(descriptor:typeParams:)")));
- (BOOL)decodeBoolean __attribute__((swift_name("decodeBoolean()")));
- (int8_t)decodeByte __attribute__((swift_name("decodeByte()")));
- (unichar)decodeChar __attribute__((swift_name("decodeChar()")));
- (double)decodeDouble __attribute__((swift_name("decodeDouble()")));
- (int32_t)decodeEnumEnumDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)enumDescriptor __attribute__((swift_name("decodeEnum(enumDescriptor:)")));
- (float)decodeFloat __attribute__((swift_name("decodeFloat()")));
- (int32_t)decodeInt __attribute__((swift_name("decodeInt()")));
- (int64_t)decodeLong __attribute__((swift_name("decodeLong()")));
- (BOOL)decodeNotNullMark __attribute__((swift_name("decodeNotNullMark()")));
- (PIAAKotlinNothing * _Nullable)decodeNull __attribute__((swift_name("decodeNull()")));
- (id _Nullable)decodeNullableSerializableValueDeserializer:(id<PIAAKotlinx_serialization_runtimeDeserializationStrategy>)deserializer __attribute__((swift_name("decodeNullableSerializableValue(deserializer:)")));
- (id _Nullable)decodeSerializableValueDeserializer:(id<PIAAKotlinx_serialization_runtimeDeserializationStrategy>)deserializer __attribute__((swift_name("decodeSerializableValue(deserializer:)")));
- (int16_t)decodeShort __attribute__((swift_name("decodeShort()")));
- (NSString *)decodeString __attribute__((swift_name("decodeString()")));
- (void)decodeUnit __attribute__((swift_name("decodeUnit()")));
- (id _Nullable)updateNullableSerializableValueDeserializer:(id<PIAAKotlinx_serialization_runtimeDeserializationStrategy>)deserializer old:(id _Nullable)old __attribute__((swift_name("updateNullableSerializableValue(deserializer:old:)")));
- (id _Nullable)updateSerializableValueDeserializer:(id<PIAAKotlinx_serialization_runtimeDeserializationStrategy>)deserializer old:(id _Nullable)old __attribute__((swift_name("updateSerializableValue(deserializer:old:)")));
@property (readonly) id<PIAAKotlinx_serialization_runtimeSerialModule> context __attribute__((swift_name("context")));
@property (readonly) PIAAKotlinx_serialization_runtimeUpdateMode *updateMode __attribute__((swift_name("updateMode")));
@end;

__attribute__((swift_name("Kotlinx_serialization_runtimeCompositeEncoder")))
@protocol PIAAKotlinx_serialization_runtimeCompositeEncoder
@required
- (void)encodeBooleanElementDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor index:(int32_t)index value:(BOOL)value __attribute__((swift_name("encodeBooleanElement(descriptor:index:value:)")));
- (void)encodeByteElementDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor index:(int32_t)index value:(int8_t)value __attribute__((swift_name("encodeByteElement(descriptor:index:value:)")));
- (void)encodeCharElementDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor index:(int32_t)index value:(unichar)value __attribute__((swift_name("encodeCharElement(descriptor:index:value:)")));
- (void)encodeDoubleElementDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor index:(int32_t)index value:(double)value __attribute__((swift_name("encodeDoubleElement(descriptor:index:value:)")));
- (void)encodeFloatElementDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor index:(int32_t)index value:(float)value __attribute__((swift_name("encodeFloatElement(descriptor:index:value:)")));
- (void)encodeIntElementDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor index:(int32_t)index value:(int32_t)value __attribute__((swift_name("encodeIntElement(descriptor:index:value:)")));
- (void)encodeLongElementDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor index:(int32_t)index value:(int64_t)value __attribute__((swift_name("encodeLongElement(descriptor:index:value:)")));
- (void)encodeNonSerializableElementDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor index:(int32_t)index value:(id)value __attribute__((swift_name("encodeNonSerializableElement(descriptor:index:value:)"))) __attribute__((unavailable("This method is deprecated for removal. Please remove it from your implementation and delegate to default method instead")));
- (void)encodeNullableSerializableElementDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor index:(int32_t)index serializer:(id<PIAAKotlinx_serialization_runtimeSerializationStrategy>)serializer value:(id _Nullable)value __attribute__((swift_name("encodeNullableSerializableElement(descriptor:index:serializer:value:)")));
- (void)encodeSerializableElementDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor index:(int32_t)index serializer:(id<PIAAKotlinx_serialization_runtimeSerializationStrategy>)serializer value:(id _Nullable)value __attribute__((swift_name("encodeSerializableElement(descriptor:index:serializer:value:)")));
- (void)encodeShortElementDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor index:(int32_t)index value:(int16_t)value __attribute__((swift_name("encodeShortElement(descriptor:index:value:)")));
- (void)encodeStringElementDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor index:(int32_t)index value:(NSString *)value __attribute__((swift_name("encodeStringElement(descriptor:index:value:)")));
- (void)encodeUnitElementDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor index:(int32_t)index __attribute__((swift_name("encodeUnitElement(descriptor:index:)")));
- (void)endStructureDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor __attribute__((swift_name("endStructure(descriptor:)")));
- (BOOL)shouldEncodeElementDefaultDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor index:(int32_t)index __attribute__((swift_name("shouldEncodeElementDefault(descriptor:index:)")));
@property (readonly) id<PIAAKotlinx_serialization_runtimeSerialModule> context __attribute__((swift_name("context")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinArray")))
@interface PIAAKotlinArray : PIAABase
+ (instancetype)arrayWithSize:(int32_t)size init:(id _Nullable (^)(PIAAInt *))init __attribute__((swift_name("init(size:init:)")));
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (id _Nullable)getIndex:(int32_t)index __attribute__((swift_name("get(index:)")));
- (id<PIAAKotlinIterator>)iterator __attribute__((swift_name("iterator()")));
- (void)setIndex:(int32_t)index value:(id _Nullable)value __attribute__((swift_name("set(index:value:)")));
@property (readonly) int32_t size __attribute__((swift_name("size")));
@end;

__attribute__((swift_name("Kotlinx_serialization_runtimeSerialModule")))
@protocol PIAAKotlinx_serialization_runtimeSerialModule
@required
- (void)dumpToCollector:(id<PIAAKotlinx_serialization_runtimeSerialModuleCollector>)collector __attribute__((swift_name("dumpTo(collector:)")));
- (id<PIAAKotlinx_serialization_runtimeKSerializer> _Nullable)getContextualKclass:(id<PIAAKotlinKClass>)kclass __attribute__((swift_name("getContextual(kclass:)")));
- (id<PIAAKotlinx_serialization_runtimeKSerializer> _Nullable)getPolymorphicBaseClass:(id<PIAAKotlinKClass>)baseClass value:(id)value __attribute__((swift_name("getPolymorphic(baseClass:value:)")));
- (id<PIAAKotlinx_serialization_runtimeKSerializer> _Nullable)getPolymorphicBaseClass:(id<PIAAKotlinKClass>)baseClass serializedClassName:(NSString *)serializedClassName __attribute__((swift_name("getPolymorphic(baseClass:serializedClassName:)")));
@end;

__attribute__((swift_name("KotlinAnnotation")))
@protocol PIAAKotlinAnnotation
@required
@end;

__attribute__((swift_name("Kotlinx_serialization_runtimeSerialKind")))
@interface PIAAKotlinx_serialization_runtimeSerialKind : PIAABase
- (NSString *)description __attribute__((swift_name("description()")));
@end;

__attribute__((swift_name("Kotlinx_serialization_runtimeCompositeDecoder")))
@protocol PIAAKotlinx_serialization_runtimeCompositeDecoder
@required
- (BOOL)decodeBooleanElementDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor index:(int32_t)index __attribute__((swift_name("decodeBooleanElement(descriptor:index:)")));
- (int8_t)decodeByteElementDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor index:(int32_t)index __attribute__((swift_name("decodeByteElement(descriptor:index:)")));
- (unichar)decodeCharElementDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor index:(int32_t)index __attribute__((swift_name("decodeCharElement(descriptor:index:)")));
- (int32_t)decodeCollectionSizeDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor __attribute__((swift_name("decodeCollectionSize(descriptor:)")));
- (double)decodeDoubleElementDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor index:(int32_t)index __attribute__((swift_name("decodeDoubleElement(descriptor:index:)")));
- (int32_t)decodeElementIndexDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor __attribute__((swift_name("decodeElementIndex(descriptor:)")));
- (float)decodeFloatElementDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor index:(int32_t)index __attribute__((swift_name("decodeFloatElement(descriptor:index:)")));
- (int32_t)decodeIntElementDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor index:(int32_t)index __attribute__((swift_name("decodeIntElement(descriptor:index:)")));
- (int64_t)decodeLongElementDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor index:(int32_t)index __attribute__((swift_name("decodeLongElement(descriptor:index:)")));
- (id _Nullable)decodeNullableSerializableElementDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor index:(int32_t)index deserializer:(id<PIAAKotlinx_serialization_runtimeDeserializationStrategy>)deserializer __attribute__((swift_name("decodeNullableSerializableElement(descriptor:index:deserializer:)")));
- (BOOL)decodeSequentially __attribute__((swift_name("decodeSequentially()")));
- (id _Nullable)decodeSerializableElementDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor index:(int32_t)index deserializer:(id<PIAAKotlinx_serialization_runtimeDeserializationStrategy>)deserializer __attribute__((swift_name("decodeSerializableElement(descriptor:index:deserializer:)")));
- (int16_t)decodeShortElementDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor index:(int32_t)index __attribute__((swift_name("decodeShortElement(descriptor:index:)")));
- (NSString *)decodeStringElementDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor index:(int32_t)index __attribute__((swift_name("decodeStringElement(descriptor:index:)")));
- (void)decodeUnitElementDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor index:(int32_t)index __attribute__((swift_name("decodeUnitElement(descriptor:index:)")));
- (void)endStructureDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor __attribute__((swift_name("endStructure(descriptor:)")));
- (id _Nullable)updateNullableSerializableElementDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor index:(int32_t)index deserializer:(id<PIAAKotlinx_serialization_runtimeDeserializationStrategy>)deserializer old:(id _Nullable)old __attribute__((swift_name("updateNullableSerializableElement(descriptor:index:deserializer:old:)")));
- (id _Nullable)updateSerializableElementDescriptor:(id<PIAAKotlinx_serialization_runtimeSerialDescriptor>)descriptor index:(int32_t)index deserializer:(id<PIAAKotlinx_serialization_runtimeDeserializationStrategy>)deserializer old:(id _Nullable)old __attribute__((swift_name("updateSerializableElement(descriptor:index:deserializer:old:)")));
@property (readonly) id<PIAAKotlinx_serialization_runtimeSerialModule> context __attribute__((swift_name("context")));
@property (readonly) PIAAKotlinx_serialization_runtimeUpdateMode *updateMode __attribute__((swift_name("updateMode")));
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("KotlinNothing")))
@interface PIAAKotlinNothing : PIAABase
@end;

__attribute__((objc_subclassing_restricted))
__attribute__((swift_name("Kotlinx_serialization_runtimeUpdateMode")))
@interface PIAAKotlinx_serialization_runtimeUpdateMode : PIAAKotlinEnum
+ (instancetype)alloc __attribute__((unavailable));
+ (instancetype)allocWithZone:(struct _NSZone *)zone __attribute__((unavailable));
- (instancetype)initWithName:(NSString *)name ordinal:(int32_t)ordinal __attribute__((swift_name("init(name:ordinal:)"))) __attribute__((objc_designated_initializer)) __attribute__((unavailable));
@property (class, readonly) PIAAKotlinx_serialization_runtimeUpdateMode *banned __attribute__((swift_name("banned")));
@property (class, readonly) PIAAKotlinx_serialization_runtimeUpdateMode *overwrite __attribute__((swift_name("overwrite")));
@property (class, readonly) PIAAKotlinx_serialization_runtimeUpdateMode *update __attribute__((swift_name("update")));
- (int32_t)compareToOther:(PIAAKotlinx_serialization_runtimeUpdateMode *)other __attribute__((swift_name("compareTo(other:)")));
@end;

__attribute__((swift_name("KotlinIterator")))
@protocol PIAAKotlinIterator
@required
- (BOOL)hasNext __attribute__((swift_name("hasNext()")));
- (id _Nullable)next __attribute__((swift_name("next()")));
@end;

__attribute__((swift_name("Kotlinx_serialization_runtimeSerialModuleCollector")))
@protocol PIAAKotlinx_serialization_runtimeSerialModuleCollector
@required
- (void)contextualKClass:(id<PIAAKotlinKClass>)kClass serializer:(id<PIAAKotlinx_serialization_runtimeKSerializer>)serializer __attribute__((swift_name("contextual(kClass:serializer:)")));
- (void)polymorphicBaseClass:(id<PIAAKotlinKClass>)baseClass actualClass:(id<PIAAKotlinKClass>)actualClass actualSerializer:(id<PIAAKotlinx_serialization_runtimeKSerializer>)actualSerializer __attribute__((swift_name("polymorphic(baseClass:actualClass:actualSerializer:)")));
@end;

__attribute__((swift_name("KotlinKDeclarationContainer")))
@protocol PIAAKotlinKDeclarationContainer
@required
@end;

__attribute__((swift_name("KotlinKAnnotatedElement")))
@protocol PIAAKotlinKAnnotatedElement
@required
@end;

__attribute__((swift_name("KotlinKClassifier")))
@protocol PIAAKotlinKClassifier
@required
@end;

__attribute__((swift_name("KotlinKClass")))
@protocol PIAAKotlinKClass <PIAAKotlinKDeclarationContainer, PIAAKotlinKAnnotatedElement, PIAAKotlinKClassifier>
@required
- (BOOL)isInstanceValue:(id _Nullable)value __attribute__((swift_name("isInstance(value:)")));
@property (readonly) NSString * _Nullable qualifiedName __attribute__((swift_name("qualifiedName")));
@property (readonly) NSString * _Nullable simpleName __attribute__((swift_name("simpleName")));
@end;

#pragma clang diagnostic pop
NS_ASSUME_NONNULL_END
