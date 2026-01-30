# Deployment Guide for DevBlocker on Render

This guide will help you deploy your DevBlocker microservices application to Render.

## Prerequisites

1. ✅ MongoDB Atlas database deployed and accessible
2. ✅ GitHub repository with your code
3. ✅ Render account (free tier available)

## Step 1: Prepare MongoDB Connection Strings

For each service, you'll need a MongoDB connection string. Format:
```
mongodb+srv://username:password@cluster.mongodb.net/database_name?retryWrites=true&w=majority
```

**Required databases:**
- `auth_db`
- `user_db`
- `team_db`
- `channel_db`
- `chat_db`
- `meeting_db`
- `file_db`
- `notification_db`
- `search_db`
- `task_db`

## Step 2: Deploy Using Render Blueprint

1. **Go to Render Dashboard**
   - Navigate to https://dashboard.render.com
   - Click "New +" → "Blueprint"

2. **Connect Your Repository**
   - Connect your GitHub repository
   - Render will detect the `render.yaml` file

3. **Review Services**
   - Render will show all 12 services (10 microservices + API Gateway + Frontend)
   - Click "Apply" to create all services

## Step 3: Configure Environment Variables

After services are created, you need to set environment variables for each service:

### For ALL Services:
- **JWT_SECRET**: Same secret key for all services (use a strong random string)
  ```
  Example: DevBlockerSecretKeyForJWTTokenGeneration123456789012345678901234567890
  ```

### For Each Microservice (Auth, User, Team, Channel, Chat, Meeting, File, Notification, Search, Task):

1. **MONGODB_URI**: MongoDB connection string with appropriate database
   ```
   mongodb+srv://gshubhamkumar01:dwMIFXB9Yne3sw5c@cluster0.htoijrv.mongodb.net/auth_db?retryWrites=true&w=majority
   ```
   (Replace `auth_db` with the correct database name for each service)

2. **MONGODB_DATABASE**: Database name (already set in render.yaml, but verify)

3. **SPRING_KAFKA_BOOTSTRAP_SERVERS** (Optional): Only if using Kafka
   ```
   Leave empty if not using Kafka
   ```

### For API Gateway:
- **JWT_SECRET**: Must match the JWT_SECRET in all services
- **FRONTEND_URL**: Your frontend URL (will be auto-set by Render)
- Service URLs are automatically set by Render from service dependencies

### For Frontend:
- **VITE_API_GATEWAY_HOST**: Auto-set by Render
- **VITE_API_GATEWAY_PORT**: 443 (already set)
- **VITE_API_GATEWAY_PROTOCOL**: https (already set)

## Step 4: Set Environment Variables in Render

For each service, go to:
1. **Service Settings** → **Environment**
2. Add the following variables:

### Auth Service:
```
MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/auth_db?retryWrites=true&w=majority
JWT_SECRET=your-secret-key-here
```

### User Service:
```
MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/user_db?retryWrites=true&w=majority
JWT_SECRET=your-secret-key-here
```

### Team Service:
```
MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/team_db?retryWrites=true&w=majority
JWT_SECRET=your-secret-key-here
```

### Channel Service:
```
MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/channel_db?retryWrites=true&w=majority
JWT_SECRET=your-secret-key-here
```

### Chat Service:
```
MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/chat_db?retryWrites=true&w=majority
JWT_SECRET=your-secret-key-here
```

### Meeting Service:
```
MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/meeting_db?retryWrites=true&w=majority
JWT_SECRET=your-secret-key-here
SPRING_KAFKA_BOOTSTRAP_SERVERS= (leave empty if not using Kafka)
```

### File Service:
```
MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/file_db?retryWrites=true&w=majority
JWT_SECRET=your-secret-key-here
```

### Notification Service:
```
MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/notification_db?retryWrites=true&w=majority
JWT_SECRET=your-secret-key-here
SPRING_KAFKA_BOOTSTRAP_SERVERS= (leave empty if not using Kafka)
```

### Search Service:
```
MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/search_db?retryWrites=true&w=majority
JWT_SECRET=your-secret-key-here
```

### Task Service:
```
MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/task_db?retryWrites=true&w=majority
JWT_SECRET=your-secret-key-here
SPRING_KAFKA_BOOTSTRAP_SERVERS= (leave empty if not using Kafka)
```

### API Gateway:
```
JWT_SECRET=your-secret-key-here (must match all services)
```

## Step 5: Activate Production Profile for API Gateway

The API Gateway needs to use the production profile. Add this environment variable:

```
SPRING_PROFILES_ACTIVE=prod
```

## Step 6: Deploy Services

1. **Manual Deploy** (if services didn't auto-deploy):
   - Go to each service
   - Click "Manual Deploy" → "Deploy latest commit"

2. **Deploy Order** (recommended):
   - Deploy microservices first (Auth, User, Team, etc.)
   - Then deploy API Gateway
   - Finally deploy Frontend

## Step 7: Verify Deployment

1. **Check Service Health**:
   - Each service should show "Live" status
   - Check logs for any errors

2. **Test API Gateway**:
   - Visit: `https://api-gateway.onrender.com/actuator/health`
   - Should return: `{"status":"UP"}`

3. **Test Frontend**:
   - Visit your frontend URL
   - Try logging in

## Troubleshooting

### Service Won't Start
- Check logs in Render dashboard
- Verify MongoDB connection string is correct
- Ensure JWT_SECRET is set and matches across services

### API Gateway Can't Reach Services
- Verify service URLs are correct in API Gateway environment variables
- Check that services are deployed and running
- Ensure API Gateway has `SPRING_PROFILES_ACTIVE=prod` set

### Frontend Can't Connect to API
- Verify `VITE_API_GATEWAY_HOST` is set correctly
- Check browser console for CORS errors
- Ensure API Gateway CORS allows your frontend URL

### MongoDB Connection Errors
- Verify connection string format
- Check MongoDB Atlas network access (allow Render IPs or 0.0.0.0/0)
- Ensure database user has correct permissions

## Important Notes

1. **Free Tier Limitations**:
   - Services spin down after 15 minutes of inactivity
   - First request after spin-down may be slow (cold start)
   - Consider upgrading for production use

2. **Service URLs**:
   - Render provides URLs like: `https://service-name.onrender.com`
   - These URLs are automatically injected as environment variables

3. **Database**:
   - MongoDB Atlas is external (not on Render)
   - Ensure MongoDB Atlas allows connections from Render IPs

4. **JWT Secret**:
   - **CRITICAL**: Use the same JWT_SECRET in ALL services
   - Generate a strong random secret (at least 64 characters)
   - Never commit secrets to Git

## Next Steps

After successful deployment:
1. Update your MongoDB Atlas network access to allow Render IPs
2. Test all functionality
3. Set up monitoring and alerts
4. Consider setting up a custom domain

## Support

If you encounter issues:
1. Check Render service logs
2. Verify all environment variables are set
3. Ensure MongoDB Atlas is accessible
4. Review the troubleshooting section above

