# Package: `com.myapp.reservations.config`

This package contains Spring configuration classes for the application's web layer (MVC, CORS, static resources).

---

## `WebConfig.java`
**Type**: `@Configuration` class implementing `WebMvcConfigurer`
**Purpose**: Configures static resource serving for uploaded files and CORS mappings for the uploads endpoint.

### Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `app.upload.dir` | `uploads` | Directory where uploaded files (avatars, business images, gallery photos) are stored |

### Methods

| Method | Purpose |
|--------|---------|
| `addResourceHandlers(ResourceHandlerRegistry)` | Maps the URL path `/uploads/**` to the local filesystem directory specified by `app.upload.dir` (resolved to absolute path). This allows uploaded images to be served as static resources. |
| `addCorsMappings(CorsRegistry)` | Enables CORS for `/uploads/**` with GET-only access from `http://localhost:5173` (Vite frontend) and `http://localhost:3000`. |

### How It Works

```
Browser requests:  GET /uploads/avatars/photo.jpg
                          |
                          v
ResourceHandler:   maps /uploads/** -> file:<absolute-path-to-uploads>/
                          |
                          v
Filesystem:        C:\...\uploads\avatars\photo.jpg  (served directly)
```

### CORS Configuration

| Setting | Value |
|---------|-------|
| URL Pattern | `/uploads/**` |
| Allowed Origins | `http://localhost:5173`, `http://localhost:3000` |
| Allowed Methods | `GET` only |

**Note**: API-level CORS is handled separately in the Spring Security configuration (`SecurityConfig`), not here. This CORS mapping only covers the static file uploads endpoint.
