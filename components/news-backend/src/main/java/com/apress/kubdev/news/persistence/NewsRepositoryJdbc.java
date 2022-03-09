package com.apress.kubdev.news.persistence;


import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.sql.DataSource;
import javax.transaction.Transactional;

import org.postgis.PGgeometry;
import org.postgresql.jdbc.PgConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apress.kubdev.news.core.NewsRepository;
import com.apress.kubdev.news.core.Rectangle;
import com.apress.kubdev.news.core.domain.model.News;

import io.agroal.pool.wrapper.ConnectionWrapper;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class NewsRepositoryJdbc implements NewsRepository{
	private static final Logger LOGGER = LoggerFactory.getLogger(NewsRepositoryJdbc.class);
	private static final String SQL_INSERT = "INSERT INTO NEWS (TITLE, LINK, DESCRIPTION, LOCATION) VALUES (?,?,?,?) RETURNING ID;";
	private static final String SQL_UPDATE = "UPDATE NEWS SET TITLE=?, LINK=?, DESCRIPTION=?, LOCATION=? WHERE ID=?;";
	private static final String NEWS_PROJECTION = "N.ID,N.TITLE,N.LINK,N.DESCRIPTION,N.LOCATION";
	private static final String SQL_FIND_BY_ID = "SELECT " + NEWS_PROJECTION +  " FROM NEWS N WHERE N.ID=?;";
	private static final String SQL_COUNT_NEWS = "SELECT COUNT(*)" + " FROM NEWS;";
	private static final String SQL_DELETE_ALL_NEWS = "DELETE FROM NEWS;";
	private static final String SQL_FIND_BY_LINK = "SELECT " + NEWS_PROJECTION +  " FROM NEWS N WHERE N.LINK=?;";
	private static final String SQL_FIND_BY_GEO = "SELECT " + NEWS_PROJECTION +  " FROM NEWS N WHERE ST_Within(N.LOCATION, ?);";
	private static final String SQL_SCHEMA = "CREATE TABLE IF NOT EXISTS NEWS (ID SERIAL PRIMARY KEY, TITLE VARCHAR(256), LINK VARCHAR(256), DESCRIPTION TEXT, LOCATION GEOMETRY(point),  UNIQUE(LINK));";
	private static final String SQL_GEO_INDEX = "CREATE INDEX IF NOT EXISTS NEWS_LOCATION_IDX ON NEWS USING GIST (LOCATION);";
	
	@Inject
	DataSource defaultDataSource;
	
	@Inject
	GeometryService geometryService;
	
	@Transactional
	void startup(@Observes StartupEvent event) { 
		try {
			LOGGER.info("Checking whether NEWS table exist on startup");
			createTableAndIndexIfNotExists();
			LOGGER.info("Check successful");
		} catch (SQLException e) {
			// We do not rethrow exception because startup will fail
			LOGGER.error("Creating NEWS table at startup failed. Deferring its creation to next SQL operation", e);
		}
	}

	private void createTableAndIndexIfNotExists() throws SQLException {
		if (!newsTableExists()) {
			LOGGER.info("NEWS table does not exist");
			LOGGER.info("Assuring that NEWS table and index exists");
			try (Connection con = defaultDataSource.getConnection();
					PreparedStatement preparedStatement = con.prepareStatement(SQL_SCHEMA + SQL_GEO_INDEX)) {
				preparedStatement.execute();
				LOGGER.info("Successfully created NEWS table and index");
			} 
		}
	}
	
	private boolean newsTableExists() throws SQLException {
		try (Connection con = defaultDataSource.getConnection()) {
			DatabaseMetaData meta = con.getMetaData();
		    ResultSet resultSet = meta.getTables(null, null, "news", new String[] {"TABLE"});
		    return resultSet.next();
		}
	}
	
	@Override
	@Transactional
	public News create(News changed) {
		LOGGER.info("Inserting into database");
		try (Connection con = defaultDataSource.getConnection();
			PreparedStatement preparedStatement = con.prepareStatement(SQL_INSERT)) {
			createTableAndIndexIfNotExists();
			preparedStatement.setString(1, changed.getTitle());
			preparedStatement.setString(2, changed.getLink().toString());
			preparedStatement.setString(3, changed.getDescription());
			changed.getLocation().ifPresentOrElse(l -> {
				try {
					preparedStatement.setObject(4, geometryService.fromLocation(l));
				} catch (SQLException e) {
					throw new RepositoryException(e);
				}
			}, () -> {
				try {
					preparedStatement.setObject(4, null);
				} catch (SQLException e) {
					throw new RepositoryException(e);
				}
			});
			ResultSet rs = preparedStatement.executeQuery();
			
			if(rs.next()) {
				return changed.persisted(rs.getLong(1));
			}
			
		} catch (SQLException e) {
			throw new RepositoryException(e);
		}
		return null;
	}
	
	@Override
	@Transactional
	public News update(News changed) {
		LOGGER.info("Updating news {} in database", changed.getId());
		try (Connection con = defaultDataSource.getConnection();
			PreparedStatement preparedStatement = con.prepareStatement(SQL_UPDATE)) {
			createTableAndIndexIfNotExists();
			preparedStatement.setString(1, changed.getTitle());
			preparedStatement.setString(2, changed.getLink().toString());
			preparedStatement.setString(3, changed.getDescription());
			preparedStatement.setLong(5, changed.getId());
			changed.getLocation().ifPresentOrElse(l -> {
				try {
					preparedStatement.setObject(4, geometryService.fromLocation(l));
				} catch (SQLException e) {
					throw new RepositoryException(e);
				}
			}, () -> {
				try {
					preparedStatement.setObject(4, null);
				} catch (SQLException e) {
					throw new RepositoryException(e);
				}
			});
			preparedStatement.executeUpdate();
			
		} catch (SQLException e) {
			throw new RepositoryException(e);
		}
		return changed;
	}

	@Override
	public List<News> findByGeometry(Rectangle bounds) {
		PGgeometry geometry = geometryService.fromRectangle(bounds);
		LOGGER.info("Find by geo {}", bounds);
		try (Connection con = defaultDataSource.getConnection();
			PreparedStatement preparedStatement = con.prepareStatement(SQL_FIND_BY_GEO)) {
			createTableAndIndexIfNotExists();
			addGeoDatatypes(con);
			preparedStatement.setObject(1, geometry);
			ResultSet resultSet = preparedStatement.executeQuery();
			List<News> newsResult = new ArrayList<>();
			while(resultSet.next()) {
				newsResult.add(fromResultSet(resultSet));
			}
			LOGGER.info("Found {}", newsResult);
			return newsResult;
		} catch (SQLException | MalformedURLException e) {
			throw new RepositoryException(e);
		}
	}
	
	@Override
	public Optional<News> findById(Long id) {
		try (Connection con = defaultDataSource.getConnection();
			PreparedStatement preparedStatement = con.prepareStatement(SQL_FIND_BY_ID)) {
			createTableAndIndexIfNotExists();
			addGeoDatatypes(con);
			preparedStatement.setObject(1, id);
			
			ResultSet resultSet = preparedStatement.executeQuery();
			if(resultSet.next()) {
				return Optional.of(fromResultSet(resultSet));
			} else {
				return Optional.empty();
			}
		} catch (SQLException | MalformedURLException e) {
			throw new RepositoryException(e);
		}
	}
	
	@Override
	public long count() {
		try (Connection con = defaultDataSource.getConnection();
			PreparedStatement preparedStatement = con.prepareStatement(SQL_COUNT_NEWS)) {
			addGeoDatatypes(con);
			ResultSet resultSet = preparedStatement.executeQuery();
			return resultSet.getLong(1);
		} catch (SQLException e) {
			throw new RepositoryException(e);
		}
	}

	private void addGeoDatatypes(Connection con) throws SQLException {
		ConnectionWrapper wrapper = (ConnectionWrapper) con;
		PgConnection pgConnection = wrapper.unwrap(PgConnection.class);
		pgConnection.addDataType("geometry",PGgeometry.class);
	}
	
	private News fromResultSet(ResultSet resultSet) throws SQLException, MalformedURLException {
		return News.builder()
				.withId(resultSet.getLong(1))
				.withTitle(resultSet.getString(2))
				.withLink(new URL(resultSet.getString(3)))
				.withDescription(resultSet.getString(4))
				.withLocation(geometryService.toLocation((PGgeometry) resultSet.getObject(5)))
				.build();
	}

	@Override
	@Transactional
	public void delete(News changed) {
		// TODO Auto-generated method stub
	}
	
	@Override
	@Transactional
	public void deleteAll()
	{
		try (Connection con = defaultDataSource.getConnection();
				PreparedStatement preparedStatement = con.prepareStatement(SQL_DELETE_ALL_NEWS)) {
			createTableAndIndexIfNotExists();
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new RepositoryException(e);
		}
	}

	@Override
	public Optional<News> findByLink(URL link) {
		try (Connection con = defaultDataSource.getConnection();
				PreparedStatement preparedStatement = con.prepareStatement(SQL_FIND_BY_LINK)) {
				createTableAndIndexIfNotExists();
				addGeoDatatypes(con);
				preparedStatement.setObject(1, link.toString());
				
				ResultSet resultSet = preparedStatement.executeQuery();
				if(resultSet.next()) {
					return Optional.of(fromResultSet(resultSet));
				} else {
					return Optional.empty();
				}
		} catch (SQLException | MalformedURLException e) {
			throw new RepositoryException(e);
		}
	}

}
