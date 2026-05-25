package com.example.camerasamsungapp

object ViewerHtml {
    fun page(): String = """
        <!doctype html>
        <html lang="en">
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
            <title>Samsung TV Photo Viewer</title>
            <style>
                html, body {
                    width: 100%;
                    height: 100%;
                    margin: 0;
                    padding: 0;
                    overflow: hidden;
                    background: #000;
                    color: #fff;
                    font-family: Arial, Helvetica, sans-serif;
                }
                #stage {
                    width: 100vw;
                    height: 100vh;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    background: #000;
                }
                #photo {
                    max-width: 100vw;
                    max-height: 100vh;
                    width: auto;
                    height: auto;
                    object-fit: contain;
                }
                #message {
                    position: fixed;
                    left: 32px;
                    bottom: 28px;
                    padding: 12px 16px;
                    border-radius: 8px;
                    background: rgba(0, 0, 0, 0.58);
                    color: rgba(255, 255, 255, 0.88);
                    font-size: 18px;
                    letter-spacing: 0.2px;
                }
            </style>
        </head>
        <body>
            <div id="stage">
                <img id="photo" alt="Latest phone capture" />
            </div>
            <div id="message">Waiting for latest phone photo…</div>
            <script>
                var img = document.getElementById('photo');
                var msg = document.getElementById('message');

                function refreshImage() {
                    var nextUrl = '/latest.jpg?t=' + Date.now();
                    img.onload = function () {
                        msg.textContent = 'Live photo feed';
                    };
                    img.onerror = function () {
                        msg.textContent = 'Waiting for photo. Capture once on the phone.';
                    };
                    img.src = nextUrl;
                }

                refreshImage();
                setInterval(refreshImage, 1000);
            </script>
        </body>
        </html>
    """.trimIndent()
}
