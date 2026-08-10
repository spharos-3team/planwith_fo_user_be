import { mediaUrl } from "../api";

export default function Avatar({ nickname, profileImage, className = "" }) {
  const src = mediaUrl(profileImage);
  const initial = (nickname || "?").slice(0, 1);
  return (
    <div className={`avatar ${className}`}>
      {src ? <img src={src} alt={nickname || "profile"} /> : initial}
    </div>
  );
}
