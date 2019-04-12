package com.sinmn.core.model.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sinmn.core.model.interfaces.BaseModelDao;

public class ModelDaoImpl implements BaseModelDao{

	private DataSource dataSource;
	
	public Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public ModelDaoImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	@Override
	public List<Map<String, Object>> select(String sql) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			ResultSetMetaData md = rs.getMetaData();
			int count = md.getColumnCount();
			Set<String> labels = new HashSet<String>();
			for(int i=1;i<=count;i++) {
				labels.add(md.getColumnLabel(i));
			}
			while(rs.next()) {
				Map<String, Object> row = new HashMap<String, Object>();
				for(String label : labels) {
					row.put(label, rs.getObject(label));
				}
				result.add(row);
			}
			ps.close();
		} catch (SQLException e) {
			logger.error("[ModelDaoImpl.select.exception]",e);
		} finally {
			if(conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}

	@Override
	public long insert(String sql) {
		Connection conn = null;
		long result = 0;
		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			result = ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			logger.error("[ModelDaoImpl.insert.exception]",e);
		} finally {
			if(conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}

	@Override
	public long update(String sql) {
		Connection conn = null;
		long result = 0;
		Boolean autoCommit = null;
		try{
			conn = dataSource.getConnection();
			autoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);
			Statement statement = conn.createStatement();
			String ss[] = sql.replace(";UPDATE", ";UPDATEUPDATE").split(";UPDATE");
			for(String s : ss) {
				statement.addBatch(s);
			}
			int affectRows[] = statement.executeBatch();
			for(int r : affectRows) {
				result+=r;
			}
			conn.commit();
			conn.setAutoCommit(autoCommit);
		} catch (SQLException e) {
			try {
				conn.rollback();
				if(autoCommit != null) {
					conn.setAutoCommit(autoCommit);
				}
			} catch (SQLException e1) {
			}
			
			logger.error("[ModelDaoImpl.update.exception]",e);
		} finally {
			if(conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}

	@Override
	public long delete(String sql) {
		long result = 0;
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			result = ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			logger.error("[ModelDaoImpl.delete.exception]",e);
		} finally {
			if(conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}
}
