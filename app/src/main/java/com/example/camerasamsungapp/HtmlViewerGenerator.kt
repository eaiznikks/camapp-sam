package com.example.camerasamsungapp

object HtmlViewerGenerator {
    fun generateViewerHtml(imageUrl: String): String {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Camera Photo Viewer</title>
                <style>
                    * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                    }
                    body {
                        background-color: #000000;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        height: 100vh;
                        font-family: Arial, sans-serif;
                        overflow: hidden;
                    }
                    .container {
                        width: 100%;
                        height: 100%;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                    }
                    img {
                        max-width: 100%;
                        max-height: 100%;
                        object-fit: contain;
                    }
                    .error {
                        color: #ff6b6b;
                        font-size: 20px;
                        text-align: center;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <img id="photoImage" src="" alt="Loading..." />
                </div>
                <script>
                    const imageUrl = "$imageUrl";
                    const photoImage = document.getElementById("photoImage");
                    
                    function updateImage() {
                        const cacheBuster = new Date().getTime();
                        const urlWithTimestamp = imageUrl + "?t=" + cacheBuster;
                        photoImage.src = urlWithTimestamp;
                    }
                    
                    // Update immediately
                    updateImage();
                    
                    // Update every 1 second
                    setInterval(updateImage, 1000);
                    
                    // Handle image errors
                    photoImage.onerror = function() {
                        console.log("Failed to load image, retrying...");
                    };
                </script>
            </body>
            </html>
        """.trimIndent()
    }
}
