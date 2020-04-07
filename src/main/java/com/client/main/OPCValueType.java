package com.client.main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OPCValueType {

	public static void main(String args[]) {
        Connection conn = null; 
        PreparedStatement pstm = null;  
        ResultSet rs = null;  
        
        try {
            String quary = "SELECT value FROM opctag";
            
            conn = DBConnection.getConnection();
            pstm = conn.prepareStatement(quary);
            rs = pstm.executeQuery();
            
            while(rs.next()){
            	Object valueObj = rs.getObject("value");
            	System.out.println(valueObj.getClass().getName());
            }
            
        } catch (SQLException sqle) {
            System.out.println("SELECT문에서 예외 발생");
            sqle.printStackTrace();
            
        }finally{	// DB 연결을 종료한다.
            try{
                if ( rs != null ){rs.close();}   
                if ( pstm != null ){pstm.close();}   
                if ( conn != null ){conn.close(); }
            }catch(Exception e){
                throw new RuntimeException(e.getMessage());
            }
            
        }
    }
}
