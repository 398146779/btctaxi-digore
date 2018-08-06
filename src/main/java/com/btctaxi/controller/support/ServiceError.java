package genesis.accounting.controller.support;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * User: guangtou
 * Date: 2018/6/10 14:37
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ServiceError extends RuntimeException {
    public int code;
    public String msg;


    public ServiceError(int code, String params) {
        this.code = code;
        this.msg = params;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

    public static ServiceError of(ErrorCodeEnum errorCode) {
        return new ServiceError(errorCode.getValue(), errorCode.getDesc());
    }

    public static ServiceError ofAndThrow(ErrorCodeEnum errorCode) throws ServiceError {
        throw of(errorCode);
    }

    public static ServiceError ofAndThrow(ErrorCodeEnum errorCode, String errorMsg) throws ServiceError {
        ServiceError serviceError = of(errorCode);
        serviceError.setMsg(serviceError.getMsg() + " " + errorMsg);
        throw serviceError;
    }
    public static ServiceError ofAndThrow(ErrorCodeEnum errorCode, Exception e ) throws ServiceError {
        ServiceError serviceError = of(errorCode);
        serviceError.setMsg(serviceError.getMsg() + " " + e.getMessage());
        throw serviceError;
    }


}
