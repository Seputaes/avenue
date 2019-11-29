package gg.sep.avenue.router.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.charset.StandardCharsets;

import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;

import gg.sep.avenue.router.AbstractRouteController;
import gg.sep.avenue.router.Body;
import gg.sep.avenue.router.GET;
import gg.sep.avenue.router.Header;
import gg.sep.avenue.router.Path;
import gg.sep.avenue.router.Query;

@SuppressWarnings({"checkstyle:MagicNumber", "checkstyle:MissingJavadocMethod", "checkstyle:MissingJavadocType"})
public class TestRouteController extends AbstractRouteController {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    public @interface UnknownAnnotation {
    }

    public TestRouteController() {
        super();
    }

    private static AwsProxyResponse body(final String body) {
        final AwsProxyResponse response = new AwsProxyResponse();
        response.setBody(body);
        return response;
    }

    @GET(paths = "/")
    public AwsProxyResponse singleSlash() {
        return new AwsProxyResponse(200);
    }

    @GET(paths = "/static")
    public AwsProxyResponse singleStaticNoParams() {
        return new AwsProxyResponse(200);
    }

    @GET(paths = "/static")
    public AwsProxyResponse singleStaticRequestFirstParam(final AwsProxyRequest request) {
        return body(request.getBody());
    }

    @GET(paths = "/static/<string:foo>")
    public AwsProxyResponse staticWithStringToken(@Path("foo") final String foo) {
        return body(foo);
    }

    @GET(paths = "/static/<string:foo>")
    public AwsProxyResponse staticWithStringTokenRequestFirstParam(final AwsProxyRequest request, @Path("foo") final String foo) {
        return body(request.getBody() + foo);
    }

    @GET(paths = {"/static/<int:id>", "/static/<int:id>/index"})
    public AwsProxyResponse multiplePaths(@Path("id") final Integer id) {
        return body(id.toString());
    }

    @GET(paths = "/<unknown:foo>")
    public AwsProxyResponse unknownTokenType(@Path("foo") final Object unknown) {
        throw new AssertionError("Method should not have been invoked!");
    }

    @GET(paths = "/")
    public String invalidReturnType() {
        return "";
    }

    @GET(paths = "/")
    public AwsProxyResponse multipleAnnotations(@Path("foo") @Query("bar") final String foo) {
        throw new AssertionError("Method should not have been invoked!");
    }

    @GET(paths = "/")
    public AwsProxyResponse noAnnotations(final String foo) {
        throw new AssertionError("Method should not have been invoked!");
    }

    @GET(paths = "/")
    public AwsProxyResponse singleQueryParameter(@Query("foo") final String foo) {
        return body(String.valueOf(foo));
    }

    @GET(paths = "/")
    public AwsProxyResponse multipleQueryParameters(@Query("foo") final String foo, @Query("baz") final String baz) {
        return body(foo + baz);
    }

    @GET(paths = "/")
    public AwsProxyResponse singleHeaderParameter(@Header("foo") final String foo) {
        return body(String.valueOf(foo));
    }

    @GET(paths = "/")
    public AwsProxyResponse multipleHeaderParameters(@Header("foo") final String foo, @Header("baz") final String baz) {
        return body(foo + baz);
    }

    @GET(paths = "/")
    public AwsProxyResponse bodyHandler(@Body final String body) {
        return body(body);
    }

    @GET(paths = "/")
    public AwsProxyResponse b64BodyHandler(@Body final byte[] body) {
        return body(new String(body, StandardCharsets.UTF_8));
    }

    public AwsProxyResponse unknownAnnotation(@UnknownAnnotation final String foo) {
        return body(String.valueOf(foo));
    }
}
