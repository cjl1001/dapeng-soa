
        package com.isuwang.soa.settle.service;

        import com.github.dapeng.core.Processor;
        import com.github.dapeng.core.Service;
        import com.github.dapeng.core.SoaGlobalTransactional;

        import java.util.concurrent.Future;

        /**
         * Autogenerated by Dapeng-Code-Generator (2.0.4)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated

        * 
        **/
        @Service(name="com.isuwang.soa.settle.service.SettleService",version = "1.0.0")
        @Processor(className = "com.isuwang.soa.settle.SettleServiceAsyncCodec$Processor")
        public interface SettleServiceAsync  extends com.github.dapeng.core.definition.AsyncService {
        
            /**
            * 
            **/
            
            
              Future<Void> createSettle( com.isuwang.soa.settle.domain.Settle settle) throws com.github.dapeng.core.SoaException;
            
          
            /**
            * 
            **/
            
            
              Future<com.isuwang.soa.settle.domain.Settle> getSettleById( Integer settleId) throws com.github.dapeng.core.SoaException;
            
          
      }
      